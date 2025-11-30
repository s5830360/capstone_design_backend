package com.example.capstone_design.controller;

import com.example.capstone_design.dto.NotificationResponse;
import com.example.capstone_design.entity.Notification;
import com.example.capstone_design.entity.NotificationType;
import com.example.capstone_design.repository.UserAccountRepository;
import com.example.capstone_design.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserAccountRepository userAccountRepository;

    private Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userAccountRepository.findByEmail(email)
                .orElseThrow()
                .getId();
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications() {
        Long myId = getCurrentUserId();
        return ResponseEntity.ok(notificationService.getNotifications(myId));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        Long myId = getCurrentUserId();
        notificationService.markAsRead(id, myId);
        return ResponseEntity.ok().build();
    }

    /** 🔔 랜덤 리마인드 알림 보내기 */
    @PostMapping("/remind")
    public ResponseEntity<Void> sendRandomRemind() {

        List<String> REMIND_MESSAGES = List.of(
                "오늘의 소리를 기록해볼까요?",
                "지금 주변 소리를 담아보세요!",
                "오늘의 감정을 남겨보는 건 어떨까요?",
                "지금 들리는 소리가 오늘을 기억하게 해줄지도 몰라요."
        );

        Long myId = getCurrentUserId(); // 현재 로그인한 유저에게만 보냄
        String msg = REMIND_MESSAGES.get((int)(Math.random() * REMIND_MESSAGES.size()));

        notificationService.push(
                myId,
                NotificationType.REMINDER,
                msg,
                null,
                null
        );

        return ResponseEntity.ok().build();
    }
}
