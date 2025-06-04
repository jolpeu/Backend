package me.jimin.springbootdeveloper.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import lombok.RequiredArgsConstructor;
import me.jimin.springbootdeveloper.dto.FileInfo;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.mongodb.client.gridfs.model.GridFSFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * GridFS를 사용하여 파일 업로드/메타데이터 조회 기능을 제공하는 서비스 레이어
 */
@Service
@RequiredArgsConstructor
public class FileService {

    // GridFS에 접근할 수 있는 템플릿
    private final GridFsTemplate gridFsTemplate;

    // GridFsTemplate 외에 추가 기능(다운로드 등)을 위해 GridFsOperations도 주입
    private final GridFsOperations gridFsOperations;

    private static final DateTimeFormatter ISO_FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault());

    /**
     * 1) MultipartFile(사용자가 올린 파일)을 GridFS에 저장
     * 2) 파일 저장 시 metadata로 uploaderUserId를 함께 심어서 저장
     *
     * @param multipartFile  업로드할 파일 객체(MultipartFile)
     * @param uploaderUserId 업로더 사용자 ID
     * @return 저장된 GridFS File ID 문자열(ObjectId.toHexString())
     * @throws IOException 파일 저장 실패 시 예외 던짐
     */
    public String uploadFile(MultipartFile multipartFile, String uploaderUserId) throws IOException {
        // 1. 메타데이터(DBObject) 생성 → 업로더 ID, contentType 등을 저장
        DBObject metadata = new BasicDBObject();
        metadata.put("uploaderUserId", uploaderUserId);
        metadata.put("contentType", multipartFile.getContentType());

        // 2. GridFS에 저장 (InputStream, 원본 파일명, contentType, metadata)
        ObjectId fileId = gridFsTemplate.store(
                multipartFile.getInputStream(),
                multipartFile.getOriginalFilename(),
                multipartFile.getContentType(),
                metadata
        );

        // 3. 저장된 파일 ID(ObjectId)를 16진수 문자열로 반환
        return fileId.toHexString();
    }

    /**
     * 3) 특정 사용자(uploaderUserId)가 업로드한 파일 목록 조회
     *    GridFS의 fs.files 콜렉션에서 metadata.uploaderUserId 필드가 일치하는 문서를 찾아서 반환
     *
     * @param uploaderUserId 업로더 사용자 ID
     * @return List<FileInfo> (프론트에 JSON 형태로 반환될 DTO 리스트)
     */
    public List<FileInfo> listFiles(String uploaderUserId) {
        // Query: metadata.uploaderUserId == uploaderUserId
        Query query = Query.query(Criteria.where("metadata.uploaderUserId").is(uploaderUserId));

        // GridFsTemplate.find(query) → GridFSFile Iterator
        List<GridFSFile> fsFiles = new ArrayList<>();
        gridFsTemplate.find(query).into(fsFiles);

        // GridFSFile → FileInfo DTO로 매핑
        List<FileInfo> result = new ArrayList<>();
        for (GridFSFile fsFile : fsFiles) {
            FileInfo info = new FileInfo();
            // GridFSFile의 ObjectId → 16진수 문자열
            info.setId(fsFile.getObjectId().toHexString());
            info.setFilename(fsFile.getFilename());
            // metadata에 put("contentType") 했으므로, 다시 꺼내올 수 있음
            if (fsFile.getMetadata() != null && fsFile.getMetadata().containsKey("contentType")) {
                info.setContentType(fsFile.getMetadata().getString("contentType"));
            } else {
                info.setContentType(fsFile.getMetadata().getString("_contentType"));
            }
            info.setLength(fsFile.getLength());
            // uploadDate를 Instant → LocalDateTime → ISO 문자열로 변환
            info.setUploadDate(ISO_FORMATTER.format(fsFile.getUploadDate().toInstant()));
            if (fsFile.getMetadata() != null && fsFile.getMetadata().containsKey("uploaderUserId")) {
                info.setUploaderUserId(fsFile.getMetadata().getString("uploaderUserId"));
            } else {
                info.setUploaderUserId("unknown");
            }
            result.add(info);
        }

        return result;
    }

    /**
     * 4) (선택) 파일 다운로드가 필요할 때 사용
     *    - 예: GridFS에서 파일을 꺼내 InputStream 형태로 클라이언트에 돌려줄 때
     *
     * @param fileIdHex GridFS File ID 문자열(ObjectId.toHexString())
     * @return Optional<InputStream> (다운로드용 InputStream)
     */
    public Optional<InputStream> getFileAsStream(String fileIdHex) throws IOException {
        ObjectId objectId = new ObjectId(fileIdHex);
        GridFSFile fsFile = gridFsTemplate.findOne(Query.query(Criteria.where("_id").is(objectId)));
        if (fsFile == null) {
            return Optional.empty();
        }
        return Optional.of(gridFsOperations.getResource(fsFile).getInputStream());
    }
}
