package me.jimin.springbootdeveloper.service;

import lombok.RequiredArgsConstructor;
import me.jimin.springbootdeveloper.domain.ReadingProgress;
import me.jimin.springbootdeveloper.dto.UpsertProgressRequest;
import me.jimin.springbootdeveloper.repository.ReadingProgressRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReadingProgressService {

    private final ReadingProgressRepository readingProgressRepository;

    // 독서 진행률 조회
    public ReadingProgress getReadingProgress(String userId, String bookId) {
        return readingProgressRepository.findByUserIdAndBookId(userId, bookId)
                .orElse(null); // 데이터가 없으면 null 반환
    }

    // 독서 진행률 저장 또는 업데이트 (Upsert)
    public ReadingProgress upsertReadingProgress(UpsertProgressRequest request) {
        // findByUserIdAndBookId로 기존 데이터가 있는지 확인
        ReadingProgress progress = readingProgressRepository
                .findByUserIdAndBookId(request.getUserId(), request.getBookId())
                .orElse(new ReadingProgress()); // 없으면 새 객체 생성

        // 요청받은 데이터로 값 설정 (신규/기존 동일)
        progress.setUserId(request.getUserId());
        progress.setBookId(request.getBookId());
        progress.setOffset(request.getOffset());
        progress.setRatio(request.getRatio());
        progress.setUpdatedAt(LocalDateTime.now());

        // JpaRepository의 save는 id가 있으면 update, 없으면 insert를 실행
        return readingProgressRepository.save(progress);
    }
}