package com.example.capstone_design.repository;

import com.example.capstone_design.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByReceiver_IdOrderByCreatedAtDesc(Long receiverId);

    long countByReceiver_IdAndIsReadFalse(Long receiverId);
}
