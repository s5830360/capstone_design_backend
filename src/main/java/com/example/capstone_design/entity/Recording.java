package com.example.capstone_design.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recording")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Recording {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // FK
    private UserAccount user;

    // =============== 좋아요 (Cascade 삭제) ===============
    @OneToMany(mappedBy = "recording", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes = new ArrayList<>();

    // 파일 메타
    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false)
    private String storedFilename;   // 서버에 저장된 파일명 (UUID 등)

    private String contentType;

    @Column(nullable = false)
    private long size;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // FastAPI 감정 분석 결과
    private String emotion;
    private Double confidence;

    // 위치 정보 (좌표 + 주소 별칭)
    private Double latitude;
    private Double longitude;
    private String address;
    private String alias;

    // 사용자 입력
    @Column(length = 255)
    private String description; // 설명글

    @Column(nullable = false)
    private boolean emotionPublic = true; // 감정 공개 여부 (기본값 : 공개)

    @Column(nullable = false)
    private boolean feedPublic = true;
}
