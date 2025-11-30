package com.example.capstone_design.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private String type;
    private String message;

    private Long recordingId;
    private Long friendUserId;

    private LocalDateTime createdAt;

    private boolean isRead;
}
