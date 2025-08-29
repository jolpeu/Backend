package me.jimin.springbootdeveloper.repository;

import me.jimin.springbootdeveloper.domain.PdfAnalysis;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PdfAnalysisRepository extends MongoRepository<PdfAnalysis, String> {

    List<PdfAnalysis> findByUserId(String userId);
}