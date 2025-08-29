package me.jimin.springbootdeveloper.repository;

import me.jimin.springbootdeveloper.domain.ReadingProgress;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReadingProgressRepository extends MongoRepository<ReadingProgress, String> {

    // userId와 bookId를 기준으로 독서 진행률 정보를 조회하는 쿼리 메소드
    Optional<ReadingProgress> findByUserIdAndBookId(String userId, String bookId);
}