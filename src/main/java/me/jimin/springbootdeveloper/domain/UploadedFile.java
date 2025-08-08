package me.jimin.springbootdeveloper.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB GridFS에 저장된 파일 메타데이터(일부, 필요 시 추가 필드 사용 가능)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "fs.files")
// GridFS를 사용할 때, 실제 파일 정보는 'fs.files' 콜렉션에 저장됨.
// 이 어노테이션은 필수는 아니지만, 문서화 용도로 붙여둘 수 있습니다.
public class UploadedFile {
    @Id
    private String id;                  // GridFS File ID (ObjectId.toHexString)

    private String filename;            // 원본 파일명

    private String contentType;         // MIME 타입 (e.g. application/pdf, image/png)

    private long length;                // 파일 사이즈 (bytes)

    private LocalDateTime uploadDate;   // 업로드 시각

    private String uploaderUserId;      // 업로더 ID (metadata.customField 등으로도 저장 가능)
    private List<String> sentences;
}
