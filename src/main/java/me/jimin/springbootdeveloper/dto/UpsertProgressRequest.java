package me.jimin.springbootdeveloper.dto;

import lombok.Getter;
import lombok.Setter;

// Flutter 앱의 PUT 요청 Body를 받기 위한 DTO
@Getter
@Setter
public class UpsertProgressRequest {
    private String userId;
    private String bookId;
    private double offset;
    private double ratio;
    private Integer currentIndex;
}