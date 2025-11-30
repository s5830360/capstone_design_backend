package com.example.capstone_design.service;

import com.example.capstone_design.dto.LikeResponse;
import com.example.capstone_design.entity.Like;
import com.example.capstone_design.entity.Recording;
import com.example.capstone_design.entity.UserAccount;
import com.example.capstone_design.entity.NotificationType;
import com.example.capstone_design.repository.LikeRepository;
import com.example.capstone_design.repository.RecordingRepository;
import com.example.capstone_design.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final RecordingRepository recordingRepository;
    private final UserAccountRepository userAccountRepository;
    private final NotificationService notificationService;

    @Transactional
    public LikeResponse like(Long recordingId, Long userId) {

        Recording recording = recordingRepository.findById(recordingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Recording not found"));

        UserAccount user = userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (likeRepository.existsByRecordingIdAndUserId(recordingId, userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already liked");
        }

        Like like = Like.builder()
                .recording(recording)
                .user(user)
                .build();

        likeRepository.save(like);

        int count = likeRepository.countByRecordingId(recordingId);

        notificationService.push(
                recording.getUser().getId(),                     // 피드 작성자
                NotificationType.LIKE_RECEIVED,                           // 알림 타입
                user.getNickname() + "님이 회원님의 게시물을 좋아합니다.",  // 메시지
                recordingId,                                     // recordingId 포함
                user.getId()                                     // 알림 보낸 사람
        );

        return new LikeResponse(recordingId, true, count);
    }

    @Transactional
    public LikeResponse unlike(Long recordingId, Long userId) {

        if (!likeRepository.existsByRecordingIdAndUserId(recordingId, userId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You haven't liked this recording yet");
        }

        likeRepository.deleteByRecordingIdAndUserId(recordingId, userId);

        int count = likeRepository.countByRecordingId(recordingId);

        return new LikeResponse(recordingId, false, count);
    }

    @Transactional(readOnly = true)
    public int count(Long recordingId) {
        return likeRepository.countByRecordingId(recordingId);
    }

    @Transactional(readOnly = true)
    public boolean isLikedByUser(Long recordingId, Long userId) {
        return likeRepository.existsByRecordingIdAndUserId(recordingId, userId);
    }
}