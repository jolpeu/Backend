package me.jimin.springbootdeveloper.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files") // 기존 구조 유지
public class EmotionController {

//    @PostMapping("/upload") // 기존 엔드포인트
    @PostMapping(
            value = "/upload",
            produces = MediaType.APPLICATION_JSON_VALUE  // application/json (아래에서 charset=UTF-8을 직접 지정)
    )
    public ResponseEntity<List<String>> uploadPdf(@RequestParam("file") MultipartFile file) throws IOException {
        String fastApiUrl = "http://127.0.0.1:8000/upload_pdf/";

        // 1) Multipart → 임시 파일 생성
        File tempFile = File.createTempFile("temp", ".pdf");
        file.transferTo(tempFile);
        FileSystemResource resource = new FileSystemResource(tempFile);

        // 2) FastAPI 호출을 위한 multipart/form-data 구성
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("file", resource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(requestBody, requestHeaders);

        // 3) FastAPI에 POST 요청
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> fastApiResponse =
                restTemplate.postForEntity(fastApiUrl, requestEntity, Map.class);

        // 4) 임시 파일 삭제
        tempFile.delete();

        // 5) FastAPI 응답에서 "results" 필드( List<Map<String,String>> ) 꺼내서 sentence만 추출
        @SuppressWarnings("unchecked")
        List<Map<String, String>> results =
                (List<Map<String, String>>) fastApiResponse.getBody().get("results");

        List<String> sentences = results.stream()
                .map(result -> result.get("sentence"))
                .toList();

        // 6) 한글 깨짐 방지를 위해, 응답 헤더에 charset=UTF-8을 명시
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(
                MediaType.parseMediaType("application/json;charset=UTF-8")
        );

        // 7) 최종적으로 문장 리스트(List<String>)를 body로, UTF-8 헤더를 붙여서 반환
        return new ResponseEntity<>(sentences, responseHeaders, HttpStatus.OK);
    }
}
