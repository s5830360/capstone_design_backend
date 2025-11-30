package com.example.capstone_design.dto;

import com.example.capstone_design.entity.Recording;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RecordingResponse {
    private Long id;

    // === 업로더 정보 ===
    private Long uploaderId;
    private String uploaderNickname;

    private String originalFilename;
    private String storedFilename;
    private String contentType;
    private long size;
    private LocalDateTime createdAt;

    // ===== 분석 결과 =====
    private String emotion;
    private Double confidence;

    // ===== 위치 정보 =====
    private Double latitude;
    private Double longitude;
    private String address;
    private String alias;

    // ===== 사용자 입력 =====
    private String description;
    private boolean emotionPublic;
    private boolean feedPublic;

    // ===== 좋아요 기능 =====
    private int likeCount;       // 총 좋아요 수
    private boolean likedByMe;   // 내가 좋아요 눌렀는지 여부

    // ===== 오디오 재생 URL =====
    private String audioUrl;

    public static RecordingResponse of(Recording r, int likeCount, boolean likedByMe) {
        String audioUrl = "/api/recordings/" + r.getId() + "/file";

        return new RecordingResponse(
                r.getId(),
                r.getUser().getId(),
                r.getUser().getNickname(),
                r.getOriginalFilename(),
                r.getStoredFilename(),
                r.getContentType(),
                r.getSize(),
                r.getCreatedAt(),
                r.getEmotion(),
                r.getConfidence(),
                r.getLatitude(),
                r.getLongitude(),
                r.getAddress(),
                r.getAlias(),
                r.getDescription(),
                r.isEmotionPublic(),
                r.isFeedPublic(),
                likeCount,
                likedByMe,
                audioUrl
        );
    }
}
