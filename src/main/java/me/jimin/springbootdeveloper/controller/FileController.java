package me.jimin.springbootdeveloper.controller;

import lombok.RequiredArgsConstructor;
import me.jimin.springbootdeveloper.dto.FileInfo;
import me.jimin.springbootdeveloper.service.FileService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 파일 업로드, 목록 조회, (선택) 다운로드 기능을 제공하는 REST 컨트롤러
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * 1) 파일 업로드 엔드포인트
     *
     * 예시 요청:
     *   POST http://localhost:8080/api/files/upload
     *   Content-Type: multipart/form-data
     *   Form-Data:
     *     ├─ file   : (파일 선택 – PDF 또는 이미지)
     *     └─ userId : anonymous  (세션/토큰 로그인 연동 전에는 기본값)
     *
     * @param file   MultipartFile (클라이언트에서 보낸 파일)
     * @param userId 업로더 사용자 ID (없으면 "anonymous")
     * @return 업로드된 파일 ID와 파일명을 담은 JSON 혹은 오류 메시지
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal String userId  // JWT 토큰의 subject(userId)가 자동으로 들어옵니다.
    ) {
        try {
            String fileId = fileService.uploadFile(file, userId);
            Map<String, String> response = new HashMap<>();
            response.put("id", fileId);
            response.put("filename", file.getOriginalFilename());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("파일 저장 중 오류 발생: " + e.getMessage());
        }
    }

    /**
     * 2) 파일 목록 조회 엔드포인트
     *
     * 예시 요청:
     *   GET http://localhost:8080/api/files/list?userId=anonymous
     *
     * @param userId 업로더 사용자 ID (없으면 "anonymous")
     * @return 업로더가 업로드한 파일 목록(List<FileInfo>)을 JSON으로 반환
     */
    @GetMapping("/list")
    public ResponseEntity<List<FileInfo>> listFiles(
            @AuthenticationPrincipal String userId
    ) {
        // JwtAuthenticationFilter가 subject(토큰의 sub)로 userId를 세팅해두었으므로,
        // userId 변수에는 토큰에 담긴 실제 사용자 ID가 담겨 있다.
        List<FileInfo> files = fileService.listFiles(userId);
        return ResponseEntity.ok(files);
    }

    /**
     * 3) (선택) 파일 다운로드 엔드포인트
     *
     * 예시 요청:
     *   GET http://localhost:8080/api/files/download/{fileId}
     *
     * @param fileId GridFS File ID (16진수 문자열)
     * @return 파일 바이너리를 스트리밍으로 내려줌 (Content-Type = 저장 시 metadata.contentType)
     */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<?> downloadFile(@PathVariable String fileId) throws IOException {
        Optional<InputStream> optionalStream = fileService.getFileAsStream(fileId);
        if (optionalStream.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        InputStreamResource resource = new InputStreamResource(optionalStream.get());

        // Content-Type은 metadata에서 가져왔으므로, FileInfo를 조회하거나
        // FileService 내부에서 contentType을 같이 리턴하도록 확장 가능
        // 여기서는 간단히 octet-stream 처리
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
