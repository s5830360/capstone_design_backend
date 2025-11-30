package com.example.capstone_design.service;

import com.example.capstone_design.dto.RecordingResponse;
import com.example.capstone_design.entity.FriendStatus;
import com.example.capstone_design.entity.Recording;
import com.example.capstone_design.entity.UserAccount;
import com.example.capstone_design.repository.FriendRepository;
import com.example.capstone_design.repository.LikeRepository;
import com.example.capstone_design.repository.RecordingRepository;
import com.example.capstone_design.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecordingService {

    private final RecordingRepository repo;
    private final LikeRepository likeRepository;
    private final AnalysisService analysisService;
    private final UserAccountRepository userAccountRepository;
    private final RecordingRepository recordingRepository;
    private final FriendService friendService;
    private final LikeService likeService;
    private final FriendRepository friendRepository;

    // ========== 파일 저장 폴더 ==========
    @Value("${storage.root:${user.home}/capstone_uploads}")
    private String storageRoot;

    private Path root() throws IOException {
        Path p = Path.of(storageRoot);
        if (!Files.exists(p)) Files.createDirectories(p);
        return p;
    }

    // =======================================================
    // 업로드
    // =======================================================
    @Transactional
    public Recording save(
            UserAccount uploader,
            MultipartFile file,
            String description,
            String emotion,
            Double latitude,
            Double longitude,
            String address,
            String alias,
            boolean emotionPublic,
            Boolean feedPublic
    ) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file is required");
        }

        try {
            String original = file.getOriginalFilename();
            String ext = (original != null && original.contains(".")) ?
                    original.substring(original.lastIndexOf('.')) : "";

            String stored = UUID.randomUUID() + ext;
            Path target = root().resolve(stored);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            var analysis = analysisService.analyzeFile(file);

            // 사용자 선택 우선
            String savedEmotion = (emotion != null && !emotion.isBlank())
                    ? emotion
                    : analysis.getFinal_emotion();

            Recording r = Recording.builder()
                    .user(uploader)
                    .originalFilename(original == null ? "unknown" : original)
                    .storedFilename(stored)
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .description(description)
                    .latitude(latitude)
                    .longitude(longitude)
                    .address(address)
                    .alias(alias)
                    .emotion(savedEmotion)
                    .confidence(analysis.getFinal_confidence())
                    .emotionPublic(emotionPublic)
                    .feedPublic(feedPublic != null ? feedPublic : true)
                    .build();

            return repo.save(r);

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "file save failed");
        }
    }

    // =======================================================
    // 기본 조회
    // =======================================================
    @Transactional(readOnly = true)
    public Optional<Recording> findById(Long id) {
        return repo.findById(id);
    }

    // =======================================================
    // DTO
    // =======================================================
    private RecordingResponse toResponse(Recording r, Long requesterId) {

        int likeCount = likeRepository.countByRecordingId(r.getId());
        boolean likedByMe = requesterId != null &&
                likeRepository.existsByRecordingIdAndUserId(r.getId(), requesterId);

        return RecordingResponse.of(r, likeCount, likedByMe);
    }

    // =======================================================
    // 내 피드 기본
    // =======================================================
    @Transactional(readOnly = true)
    public List<RecordingResponse> findMyRecordings(Long userId) {
        return repo.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(r -> toResponse(r, userId))
                .toList();
    }

    // =======================================================
    // 감정별 필터
    // =======================================================
    @Transactional(readOnly = true)
    public List<RecordingResponse> findMyByEmotion(Long userId, String emotion) {
        return repo.findByUserIdAndEmotionOrderByCreatedAtDesc(userId, emotion).stream()
                .map(r -> toResponse(r, userId))
                .toList();
    }

    // =======================================================
    // 날짜별 필터
    // =======================================================
    @Transactional(readOnly = true)
    public List<RecordingResponse> findMyByDate(Long userId, LocalDate date) {

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        return repo.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                        userId, start, end
                ).stream()
                .map(r -> toResponse(r, userId))
                .toList();
    }

    // =======================================================
    // 위치별 필터
    // =======================================================
    @Transactional(readOnly = true)
    public List<RecordingResponse> findMyByBoundingBox(
            Long userId,
            Double minLat,
            Double maxLat,
            Double minLng,
            Double maxLng
    ) {
        return repo.findMyByBoundingBox(userId, minLat, maxLat, minLng, maxLng)
                .stream()
                .map(r -> toResponse(r, userId))
                .toList();
    }

    // =======================================================
    // 공개 피드
    // =======================================================
    @Transactional(readOnly = true)
    public List<RecordingResponse> findPublicRecordings(Long requesterId) {
        return repo.findByFeedPublicTrueOrderByCreatedAtDesc().stream()
                .map(r -> toResponse(r, requesterId))
                .toList();
    }

    // =======================================================
    // 단일 조회 DTO
    // =======================================================
    @Transactional(readOnly = true)
    public RecordingResponse getDtoById(Long id) {

        Recording r = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Long requesterId = null;

        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            // principal이 UserAccount가 아니라 String 이므로
            String email = auth.getName();
            requesterId = userAccountRepository.findByEmail(email)
                    .map(UserAccount::getId)
                    .orElse(null);
        }

        return toResponse(r, requesterId);
    }

    // =======================================================
    // 내 피드 삭제
    // =======================================================
    @Transactional
    public void deleteRecording(Long recordingId, Long userId) {

        Recording recording = repo.findById(recordingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        // 본인이 올린 것만 삭제 가능
        if (!recording.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot delete this recording.");
        }

        repo.delete(recording);
    }

    // =======================================================
    // 친구 피드
    // =======================================================
    @Transactional(readOnly = true)
    public List<RecordingResponse> getFriendsFeed(Long myId) {

        // 1) 내 친구 목록 userId 리스트 가져오기
        List<Long> friendIds = friendService.getMyFriendIds(myId); // friendUserId만 리스트로

        if (friendIds.isEmpty()) {
            return List.of(); // 친구 없으면 빈 리스트
        }

        // 2) friend들이 올린 feedPublic=true 피드 조회
        List<Recording> records = recordingRepository
                .findAllByUserIdInAndFeedPublicTrueOrderByCreatedAtDesc(friendIds);

        // 3) 좋아요 정보 포함 변환
        return records.stream()
                .map(r -> {
                    int likeCount = likeService.count(r.getId());
                    boolean likedByMe = likeService.isLikedByUser(r.getId(), myId);
                    return RecordingResponse.of(r, likeCount, likedByMe);
                }).toList();
    }

    // =======================================================
    // 친구+내 피드
    // =======================================================
    @Transactional(readOnly = true)
    public List<RecordingResponse> getHomeFeed(Long myId) {

        // 1) 내 친구들의 ID 목록 가져오기
        List<Long> friendIds = friendService.getMyFriendIds(myId);

        // 2) 친구 ID + 내 ID 합치기
        List<Long> targetUserIds = new ArrayList<>(friendIds);
        targetUserIds.add(myId);

        // 3) Repository에서 한 번에 조회
        List<Recording> recordings =
                recordingRepository.findAllByUserIdInAndFeedPublicTrueOrderByCreatedAtDesc(targetUserIds);

        // 4) DTO 변환
        return recordings.stream()
                .map(r -> RecordingResponse.of(
                        r,
                        likeService.count(r.getId()),
                        likeService.isLikedByUser(r.getId(), myId)
                ))
                .toList();
    }

    // =======================================================
    // 특정 친구 피드만 조회
    // =======================================================
    @Transactional(readOnly = true)
    public List<RecordingResponse> getFriendFeed(Long myId, String nickname) {

        // 조회 대상 유저
        UserAccount friend = userAccountRepository.findByNickname(nickname)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Long friendId = friend.getId();

        // 내가 이 친구와 친구인지 확인
        boolean isFriend = friendRepository.existsFriendship(myId, friendId, FriendStatus.ACCEPTED);
        if (!isFriend && !myId.equals(friendId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not a friend");
        }

        // 친구 피드만 조회 (feedPublic = true)
        List<Recording> list = recordingRepository
                .findByUserIdAndFeedPublicTrueOrderByCreatedAtDesc(friendId);

        return list.stream()
                .map(r -> RecordingResponse.of(
                        r,
                        likeService.count(r.getId()),
                        likeService.isLikedByUser(r.getId(), myId)
                ))
                .toList();
    }

}