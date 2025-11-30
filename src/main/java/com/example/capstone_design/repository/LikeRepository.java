package com.example.capstone_design.repository;

import com.example.capstone_design.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {

    // 좋아요 여부 확인
    boolean existsByRecordingIdAndUserId(Long recordingId, Long userId);

    // 좋아요 개수
    int countByRecordingId(Long recordingId);

    // 좋아요 취소
    void deleteByRecordingIdAndUserId(Long recordingId, Long userId);
}
