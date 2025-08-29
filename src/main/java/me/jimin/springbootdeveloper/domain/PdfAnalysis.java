package me.jimin.springbootdeveloper.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

// 최상위 JSON 구조를 나타내는 클래스. MongoDB의 'analyses' 컬렉션에 저장됩니다.
@Document(collection = "analyses")
@Getter
@Setter
@Builder
public class PdfAnalysis {
    @Id
    private String id; // MongoDB의 _id 필드와 매핑

    @Field("user_id") // DB 필드명을 'user_id'로 지정
    private String userId;

    @Field("pdf_name")
    private String filename;

    @Field("uploaded_time")
    private LocalDateTime uploadedTime;

    private List<AnalysisResult> results;
}