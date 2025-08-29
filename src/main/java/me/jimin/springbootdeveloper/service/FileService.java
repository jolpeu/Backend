package me.jimin.springbootdeveloper.service;

import lombok.RequiredArgsConstructor;
import me.jimin.springbootdeveloper.domain.AnalysisResult;
import me.jimin.springbootdeveloper.domain.PdfAnalysis;
import me.jimin.springbootdeveloper.repository.PdfAnalysisRepository;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileService {

    private final PdfAnalysisRepository pdfAnalysisRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String FASTAPI_URL = "http://127.0.0.1:8000/upload_pdf/";

    /**
     * PDF 파일을 FastAPI로 보내 분석하고, 그 결과를 MongoDB 컬렉션에 저장
     */
    public PdfAnalysis analyzeAndSavePdf(MultipartFile multipartFile, String userId) throws IOException {
        // 1. MultipartFile을 임시 파일로 변환 (기존 로직과 동일)
        File tmp = File.createTempFile("upload-", ".pdf");
        multipartFile.transferTo(tmp);

        // 2. FastAPI 호출: multipart/form-data로 PDF 전송 (기존 로직과 동일)
        var body = new LinkedMultiValueMap<String, Object>();
        body.add("file", new FileSystemResource(tmp));

        // FastAPI 응답을 Map으로 받음
        @SuppressWarnings("unchecked")
        Map<String, Object> fastApiResp = restTemplate.postForObject(FASTAPI_URL, body, Map.class);

        tmp.delete(); // 임시 파일 즉시 삭제

        if (fastApiResp == null || !fastApiResp.containsKey("results")) {
            throw new RuntimeException("FastAPI 응답에 'results' 키가 없습니다.");
        }

        // 3. FastAPI 응답(List<Map>)을 List<AnalysisResult>로 변환
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawResults = (List<Map<String, Object>>) fastApiResp.get("results");

        List<AnalysisResult> results = rawResults.stream()
                .map(item -> AnalysisResult.builder()
                        .sentence((String) item.get("sentence"))
                        // FastAPI가 아래 데이터도 반환한다고 가정합니다.
                        .emotion((String) item.getOrDefault("emotion", "neutral"))
                        .effectFile((String) item.getOrDefault("effect_file", ""))
                        .ttsFile((String) item.getOrDefault("tts_file", ""))
                        .build())
                .collect(Collectors.toList());

        // 4. 최종 PdfAnalysis 객체 생성
        PdfAnalysis analysis = PdfAnalysis.builder()
                .userId(userId)
                .filename(multipartFile.getOriginalFilename())
                .uploadedTime(LocalDateTime.now())
                .results(results)
                .build();

        return pdfAnalysisRepository.save(analysis);
    }

    public List<PdfAnalysis> listAnalysesByUserId(String userId) {
        return pdfAnalysisRepository.findByUserId(userId);
    }
}