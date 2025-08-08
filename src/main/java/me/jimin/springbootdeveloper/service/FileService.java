package me.jimin.springbootdeveloper.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import lombok.RequiredArgsConstructor;
import me.jimin.springbootdeveloper.dto.FileInfo;
import org.bson.types.ObjectId;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileService {

    private final GridFsTemplate gridFsTemplate;
    private final GridFsOperations gridFsOperations;
    private final RestTemplate restTemplate = new RestTemplate();

    // FastAPI 업로드 엔드포인트
    private static final String FASTAPI_URL = "http://127.0.0.1:8000/upload_pdf/";

    /**
     * 1) MultipartFile을 GridFS에 저장하면서
     *    FastAPI에 PDF 보내 추출된 문장 리스트를 metadata에 함께 심음
     */
    public String uploadFile(MultipartFile multipartFile, String uploaderUserId) throws IOException {
        // 1) MultipartFile → 임시 파일
        File tmp = File.createTempFile("upload-", ".pdf");
        multipartFile.transferTo(tmp);

        // 2) FastAPI 호출: multipart/form-data 로 PDF 전송
        var body = new LinkedMultiValueMap<String, Object>();
        body.add("file", new FileSystemResource(tmp));

        @SuppressWarnings("unchecked")
        Map<String, Object> fastApiResp = restTemplate.postForObject(
                FASTAPI_URL,        // "http://127.0.0.1:8000/upload_pdf/"
                body,
                Map.class
        );

        if (fastApiResp == null || !fastApiResp.containsKey("results")) {
            tmp.delete();
            throw new RuntimeException("FastAPI 응답에 results 키가 없습니다.");
        }

        // 3) results 배열에서 sentence들만 추출
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawResults =
                (List<Map<String, Object>>) fastApiResp.get("results");
        List<String> sentences = rawResults.stream()
                .map(m -> m.get("sentence").toString())
                .collect(Collectors.toList());

        // 4) GridFS metadata 에 심기
        DBObject metadata = new BasicDBObject();
        metadata.put("uploaderUserId", uploaderUserId);
        metadata.put("contentType", multipartFile.getContentType());
        metadata.put("sentences", sentences);

        // 5) GridFS에 저장 (InputStream으로 읽고 난 뒤에 tmp 파일 삭제)
        ObjectId fileId = gridFsTemplate.store(
                new FileInputStream(tmp),
                multipartFile.getOriginalFilename(),
                multipartFile.getContentType(),
                metadata
        );
        tmp.delete();

        return fileId.toHexString();
    }
//    public String uploadFile(MultipartFile multipartFile, String uploaderUserId) throws IOException {
//        // 1. MultipartFile → 임시 파일
//        File tmp = File.createTempFile("upload-", ".pdf");
//        multipartFile.transferTo(tmp);
//
//        // 2. FastAPI 호출: multipart/form-data 로 PDF 전송
//        var body = new org.springframework.util.LinkedMultiValueMap<String, Object>();
//        body.add("file", new FileSystemResource(tmp));
//        @SuppressWarnings("unchecked")
//        Map<String, Object> fastApiResp = restTemplate.postForObject(FASTAPI_URL, body, Map.class);
//
//
//        if (fastApiResp == null || !fastApiResp.containsKey("results")) {
//            throw new RuntimeException("FastAPI 응답이 유효하지 않습니다.");
//        }
//
//        // 3. 응답에서 문장 리스트 추출
//        @SuppressWarnings("unchecked")
//        List<String> sentences = restTemplate.postForObject(
//                FASTAPI_URL, body, List.class);
//
//        if (sentences == null) {
//            throw new RuntimeException("FastAPI에서 문장 리스트를 받지 못했습니다.");
//        }
//
//// 바로 sentences 변수 사용
//        DBObject metadata = new BasicDBObject();
//        metadata.put("uploaderUserId", uploaderUserId);
//        metadata.put("contentType", multipartFile.getContentType());
//        metadata.put("sentences", sentences);
//
//
//        // 5. GridFS에 저장
//        ObjectId fileId = gridFsTemplate.store(
//                multipartFile.getInputStream(),
//                multipartFile.getOriginalFilename(),
//                multipartFile.getContentType(),
//                metadata
//        );
//
//        tmp.delete();
//
//        return fileId.toHexString();
//    }

    /**
     * 2) uploaderUserId에 해당하는 파일 목록 조회
     *    metadata.uploaderUserId 로 필터, metadata.sentences 도 함께 읽어 DTO로 변환
     */
    public List<FileInfo> listFiles(String uploaderUserId) {
        // metadata.uploaderUserId == uploaderUserId
        Query query = Query.query(Criteria.where("metadata.uploaderUserId").is(uploaderUserId));
        List<GridFSFile> fsFiles = new ArrayList<>();
        gridFsTemplate.find(query).into(fsFiles);

        List<FileInfo> result = new ArrayList<>();
        for (GridFSFile fsFile : fsFiles) {
            FileInfo info = new FileInfo();
            info.setId(fsFile.getObjectId().toHexString());
            info.setFilename(fsFile.getFilename());
            // contentType
            if (fsFile.getMetadata() != null && fsFile.getMetadata().containsKey("contentType")) {
                info.setContentType(fsFile.getMetadata().getString("contentType"));
            } else {
                info.setContentType("application/octet-stream");
            }
            info.setLength(fsFile.getLength());
            info.setUploadDate(fsFile.getUploadDate().toInstant().toString());
            // uploaderUserId
            if (fsFile.getMetadata() != null && fsFile.getMetadata().containsKey("uploaderUserId")) {
                info.setUploaderUserId(fsFile.getMetadata().getString("uploaderUserId"));
            } else {
                info.setUploaderUserId("unknown");
            }
            // sentences
            if (fsFile.getMetadata() != null && fsFile.getMetadata().containsKey("sentences")) {
                @SuppressWarnings("unchecked")
                List<String> sentences = (List<String>) fsFile.getMetadata().get("sentences");
                info.setSentences(sentences);
            } else {
                info.setSentences(Collections.emptyList());
            }
            result.add(info);
        }

        return result;
    }

    /**
     * 3) 파일 다운로드용 InputStream 반환
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
