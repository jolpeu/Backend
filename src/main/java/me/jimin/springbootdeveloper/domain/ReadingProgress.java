package me.jimin.springbootdeveloper.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "reading_progress") // MongoDB 컬렉션 이름 지정
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// userId와 bookId 조합을 유니크 키로 설정하여 데이터 중복 방지
@CompoundIndex(name = "user_book_idx", def = "{'userId' : 1, 'bookId' : 1}", unique = true)
public class ReadingProgress {

    @Id
    private String id; // MongoDB 문서의 고유 ID

    private String userId;
    private String bookId;

    private double offset; // 스크롤 위치
    private double ratio;  // 진행률 (0.0 ~ 1.0)
    private Integer currentIndex;

    private LocalDateTime updatedAt; // 마지막 업데이트 시간
}