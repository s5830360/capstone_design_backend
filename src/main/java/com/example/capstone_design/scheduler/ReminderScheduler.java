package com.example.capstone_design.scheduler;

import com.example.capstone_design.entity.NotificationType;
import com.example.capstone_design.entity.UserAccount;
import com.example.capstone_design.repository.UserAccountRepository;
import com.example.capstone_design.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReminderScheduler {

    private final UserAccountRepository userAccountRepository;
    private final NotificationService notificationService;

    private final List<String> REMIND_MESSAGES = List.of(
            "오늘의 소리를 기록해볼까요?",
            "지금 주변 소리를 담아보세요!",
            "오늘의 감정을 남겨보는 건 어떨까요?",
            "지금 들리는 소리가 오늘을 기억하게 해줄지도 몰라요."
    );

    /** 매일 오전 10시 */
    @Scheduled(cron = "0 0 10 * * *", zone = "Asia/Seoul")
    public void sendMorningReminder() {
        sendReminderToAllUsers();
    }

    /** 매일 오후 5시 30분 */
    @Scheduled(cron = "0 30 17 * * *", zone = "Asia/Seoul")
    public void sendEveningReminder() {
        sendReminderToAllUsers();
    }

    private void sendReminderToAllUsers() {
        log.info("🔔 Sending reminder to all users...");

        List<UserAccount> allUsers = userAccountRepository.findAll();
        String msg = REMIND_MESSAGES.get((int)(Math.random() * REMIND_MESSAGES.size()));

        for (UserAccount user : allUsers) {
            notificationService.push(
                    user.getId(),
                    NotificationType.REMINDER,
                    msg,
                    null,
                    null
            );
        }

        log.info("▶ Completed sending reminders to {} users", allUsers.size());
    }
}
