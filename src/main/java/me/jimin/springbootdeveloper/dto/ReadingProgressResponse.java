package me.jimin.springbootdeveloper.dto;

import lombok.Getter;
import me.jimin.springbootdeveloper.domain.ReadingProgress;

import java.time.LocalDateTime;

// GET, PUT 응답으로 클라이언트에 전달할 DTO
@Getter
public class ReadingProgressResponse {
    private final String userId;
    private final String bookId;
    private final double offset;
    private final double ratio;
    private final LocalDateTime updatedAt;

    // Entity 객체를 DTO로 변환하는 생성자
    public ReadingProgressResponse(ReadingProgress progress) {
        this.userId = progress.getUserId();
        this.bookId = progress.getBookId();
        this.offset = progress.getOffset();
        this.ratio = progress.getRatio();
        this.updatedAt = progress.getUpdatedAt();
    }
}