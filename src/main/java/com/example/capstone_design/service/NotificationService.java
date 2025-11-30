package com.example.capstone_design.service;

import com.example.capstone_design.dto.NotificationResponse;
import com.example.capstone_design.entity.*;
import com.example.capstone_design.repository.NotificationRepository;
import com.example.capstone_design.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserAccountRepository userAccountRepository;

    /** 알림 생성 */
    @Transactional
    public void push(Long receiverId, NotificationType type, String message,
                     Long recordingId, Long friendUserId) {

        UserAccount receiver = userAccountRepository.findById(receiverId)
                .orElseThrow();

        Notification noti = Notification.builder()
                .receiver(receiver)
                .type(type)
                .message(message)
                .recordingId(recordingId)
                .friendUserId(friendUserId)
                .isRead(false)
                .build();

        notificationRepository.save(noti);
    }

    /** 나의 모든 알림 조회 */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Long userId) {

        return notificationRepository.findByReceiver_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(n -> new NotificationResponse(
                        n.getId(),
                        n.getType().name(),
                        n.getMessage(),
                        n.getRecordingId(),
                        n.getFriendUserId(),
                        n.getCreatedAt(),
                        n.isRead()
                ))
                .toList();
    }

    /**
     * 읽음 처리
     */
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow();

        if (!n.getReceiver().getId().equals(userId)) {
            throw new RuntimeException("You cannot modify this notification");
        }

        n.setRead(true);
    }
}
