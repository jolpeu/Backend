package me.jimin.springbootdeveloper.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

// 'results' 배열의 각 요소를 나타내는 클래스
@Getter
@Setter
@Builder
public class AnalysisResult {
    private String sentence;
    private String emotion;
    private String effectFile; // 효과음 파일 경로 또는 ID
    private String ttsFile;    // TTS 음성 파일 경로 또는 ID
}