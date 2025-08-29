package me.jimin.springbootdeveloper.controller;

import lombok.RequiredArgsConstructor;
import me.jimin.springbootdeveloper.domain.PdfAnalysis;
import me.jimin.springbootdeveloper.dto.PdfAnalysisResponse;
import me.jimin.springbootdeveloper.service.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    // 엔드포인트 URL은 기존과 동일
    @PostMapping("/analyze-pdf")
    public ResponseEntity<?> analyzePdf(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        try {
            String userId = authentication.getName();

            // 1. 확장된 서비스 메소드 호출
            PdfAnalysis savedAnalysis = fileService.analyzeAndSavePdf(file, userId);

            // 2. 결과를 응답 DTO로 변환
            PdfAnalysisResponse response = new PdfAnalysisResponse(savedAnalysis);

            // 3. 생성된 DTO를 CREATED(201) 상태와 함께 반환
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("파일 처리 중 오류 발생: " + e.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<PdfAnalysis>> listFiles(Authentication authentication) {
        String userId = authentication.getName();
        List<PdfAnalysis> analyses = fileService.listAnalysesByUserId(userId);
        return ResponseEntity.ok(analyses);
    }
}