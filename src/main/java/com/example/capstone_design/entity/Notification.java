package com.example.capstone_design.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 알림을 받는 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private UserAccount receiver;

    // 알림 타입
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    // 관련된 기록 ID (optional)
    private Long recordingId;

    // 관련된 친구 ID (optional)
    private Long friendUserId;

    private String message;

    @CreationTimestamp
    private LocalDateTime createdAt;

    // 읽음 여부
    @Column(name = "is_read", nullable = false)
    private boolean isRead;
}
