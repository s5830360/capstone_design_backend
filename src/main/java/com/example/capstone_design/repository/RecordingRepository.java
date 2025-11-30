package com.example.capstone_design.repository;

import com.example.capstone_design.entity.Recording;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface RecordingRepository extends JpaRepository<Recording, Long> {
    // ============================
    // 기본 조회
    // ============================
    List<Recording> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Recording> findByFeedPublicTrueOrderByCreatedAtDesc();


    // ============================
    // 감정 필터링 (내 피드)
    // ============================
    List<Recording> findByUserIdAndEmotionOrderByCreatedAtDesc(Long userId, String emotion);


    // ============================
    // 날짜 필터링 (내 피드)
    // createdAt between date start-end
    // ============================
    List<Recording> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );

    // ================================
    // 지도 필터링 (내 피드)
    // ================================
    @Query("""
        SELECT r FROM Recording r
        WHERE r.user.id = :userId
          AND r.latitude IS NOT NULL
          AND r.longitude IS NOT NULL
          AND r.latitude BETWEEN :minLat AND :maxLat
          AND r.longitude BETWEEN :minLng AND :maxLng
        ORDER BY r.createdAt DESC
    """)
    List<Recording> findMyByBoundingBox(
            Long userId,
            Double minLat,
            Double maxLat,
            Double minLng,
            Double maxLng
    );

    // ================================
    // 친구+내 피드
    // ================================
    List<Recording> findAllByUserIdInAndFeedPublicTrueOrderByCreatedAtDesc(List<Long> userIds);

    // ================================
    // 특정 친구 피드
    // ================================
    List<Recording> findByUserIdAndFeedPublicTrueOrderByCreatedAtDesc(Long userId);

}