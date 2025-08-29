package me.jimin.springbootdeveloper.controller;

import lombok.RequiredArgsConstructor;
import me.jimin.springbootdeveloper.domain.ReadingProgress;
import me.jimin.springbootdeveloper.dto.ReadingProgressResponse;
import me.jimin.springbootdeveloper.dto.UpsertProgressRequest;
import me.jimin.springbootdeveloper.service.ReadingProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reading-progress")
public class ReadingProgressController {

    private final ReadingProgressService readingProgressService;

    // GET /reading-progress?userId=...&bookId=...
    @GetMapping
    public ResponseEntity<ReadingProgressResponse> getProgress(
            @RequestParam String userId,
            @RequestParam String bookId) {

        ReadingProgress progress = readingProgressService.getReadingProgress(userId, bookId);

        // Flutter 코드는 데이터가 없어도 200 OK를 기대하므로,
        // progress가 null이어도 ok()로 응답하고 body는 비워둠.
        if (progress == null) {
            return ResponseEntity.ok().body(null);
        }

        return ResponseEntity.ok(new ReadingProgressResponse(progress));
    }

    // PUT /reading-progress
    @PutMapping
    public ResponseEntity<ReadingProgressResponse> upsertProgress(
            @RequestBody UpsertProgressRequest request) {

        ReadingProgress savedProgress = readingProgressService.upsertReadingProgress(request);
        return ResponseEntity.ok(new ReadingProgressResponse(savedProgress));
    }
}