package com.example.capstone_design.controller;

import com.example.capstone_design.dto.RecordingResponse;
import com.example.capstone_design.entity.Recording;
import com.example.capstone_design.entity.UserAccount;
import com.example.capstone_design.repository.UserAccountRepository;
import com.example.capstone_design.service.RecordingService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/recordings")
@RequiredArgsConstructor
public class RecordingController {

    private final RecordingService recordingService;
    private final UserAccountRepository userAccountRepository;

    // =========================================
    // 업로드
    // =========================================
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RecordingResponse upload(
            @RequestParam("file") MultipartFile file,
            @RequestPart("emotion") String emotion,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "alias", required = false) String alias,
            @RequestParam(value = "emotionPublic", defaultValue = "true") boolean emotionPublic,
            @RequestParam(value = "feedPublic", required = false) Boolean feedPublic
    ) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        UserAccount user = userAccountRepository.findByEmail(email)
                .orElseThrow();

        Recording saved = recordingService.save(
                user, file, description, emotion, latitude, longitude, address, alias, emotionPublic, feedPublic);

        return recordingService.getDtoById(saved.getId());
    }

    // =========================================
    // 파일 스트리밍
    // =========================================
    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> getFile(@PathVariable Long id) {

        Recording recording = recordingService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Path filePath = Paths.get(System.getProperty("user.home"), "capstone_uploads", recording.getStoredFilename());

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(recording.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + recording.getOriginalFilename() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "File error");
        }
    }

    // =========================================
    // 단일 조회
    // =========================================
    @GetMapping("/{id}")
    public RecordingResponse getRecording(@PathVariable Long id) {
        return recordingService.getDtoById(id);
    }

    // =========================================
    // 내 피드 + 감정/날짜 필터
    // =========================================
    @GetMapping("/me")
    public List<RecordingResponse> myFeed(
            @RequestParam(value = "emotion", required = false) String emotion,
            @RequestParam(value = "date", required = false) String date, // yyyy-MM-dd
            @RequestParam(value = "minLat", required = false) Double minLat,
            @RequestParam(value = "maxLat", required = false) Double maxLat,
            @RequestParam(value = "minLng", required = false) Double minLng,
            @RequestParam(value = "maxLng", required = false) Double maxLng
    ) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = userAccountRepository.findByEmail(email)
                .orElseThrow()
                .getId();

        if (emotion != null) {
            return recordingService.findMyByEmotion(userId, emotion);
        }

        if (date != null) {
            return recordingService.findMyByDate(userId, LocalDate.parse(date));
        }

        boolean hasBoundingBox = (minLat != null && maxLat != null && minLng != null && maxLng != null);
        if (hasBoundingBox) {
            return recordingService.findMyByBoundingBox(userId, minLat, maxLat, minLng, maxLng);
        }

        return recordingService.findMyRecordings(userId);
    }

    // =========================================
    // 공개 피드
    // =========================================
    @GetMapping("/public")
    public List<RecordingResponse> publicFeed() {

        Long requesterId = null;

        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String email = auth.getName();
            requesterId = userAccountRepository.findByEmail(email)
                    .map(user -> user.getId())
                    .orElse(null);
        }

        return recordingService.findPublicRecordings(requesterId);
    }

    // =========================================
    // 내 피드 삭제
    // =========================================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecording(@PathVariable Long id) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = userAccountRepository.findByEmail(email)
                .orElseThrow()
                .getId();

        recordingService.deleteRecording(id, userId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    // =========================================
    // 친구 피드 조회
    // =========================================
    @GetMapping("/friends")
    public ResponseEntity<List<RecordingResponse>> getFriendsFeed() {
        Long myId = getCurrentUserId();
        List<RecordingResponse> feed = recordingService.getFriendsFeed(myId);
        return ResponseEntity.ok(feed);
    }

    // =========================================
    // 친구+내 피드 조회
    // =========================================
    @GetMapping("/home")
    public List<RecordingResponse> homeFeed() {
        Long myId = getCurrentUserId();
        return recordingService.getHomeFeed(myId);
    }

    private Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userAccountRepository.findByEmail(email)
                .orElseThrow()
                .getId();
    }

    // =========================================
    // 특정 친구 피드만 조회
    // =========================================
    @GetMapping("/friend/{nickname}")
    public ResponseEntity<List<RecordingResponse>> getFriendFeed(@PathVariable String nickname) {
        Long myId = getCurrentUserId();
        List<RecordingResponse> feed = recordingService.getFriendFeed(myId, nickname);
        return ResponseEntity.ok(feed);
    }
}
