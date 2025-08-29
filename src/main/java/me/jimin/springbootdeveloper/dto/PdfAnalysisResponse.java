package me.jimin.springbootdeveloper.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import me.jimin.springbootdeveloper.domain.AnalysisResult;
import me.jimin.springbootdeveloper.domain.PdfAnalysis;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PdfAnalysisResponse {
    @JsonProperty("_id") // JSON으로 변환될 때 필드명을 '_id'로 지정
    private final String id;

    @JsonProperty("user_id")
    private final String userId;

    @JsonProperty("pdf_name")
    private final String filename;

    @JsonProperty("uploaded_time")
    private final LocalDateTime uploadedTime;

    private final List<AnalysisResult> results;

    // 도메인 객체를 DTO로 변환하는 생성자
    public PdfAnalysisResponse(PdfAnalysis entity) {
        this.id = entity.getId();
        this.userId = entity.getUserId();
        this.filename = entity.getFilename();
        this.uploadedTime = entity.getUploadedTime();
        this.results = entity.getResults();
    }
}