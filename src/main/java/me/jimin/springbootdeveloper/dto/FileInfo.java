package me.jimin.springbootdeveloper.dto;

import lombok.*;

/**
 * 클라이언트(Flutter 혹은 프론트)에서 "내 서재" 화면을 구성할 때
 * 필요한 파일 목록(메타데이터) 정보를 담아서 전달하기 위한 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileInfo {
    private String id;            // GridFS File ID (ObjectId.toHexString)
    private String filename;      // 원본 파일명
    private String contentType;   // MIME 타입
    private long length;          // 파일 사이즈 (bytes)
    private String uploadDate;    // 업로드 일시를 ISO 포맷 문자열로 변환 (ex: "2025-06-02T22:15:30")
    private String uploaderUserId;// 업로더 ID (나중에 인증 연동 시 사용)
}
