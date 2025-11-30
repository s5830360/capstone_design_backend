package com.example.capstone_design.controller;

import com.example.capstone_design.dto.FriendListDTO;
import com.example.capstone_design.dto.FriendRequestDTO;
import com.example.capstone_design.dto.FriendResponseDTO;
import com.example.capstone_design.dto.UserSimpleDTO;
import com.example.capstone_design.repository.UserAccountRepository;
import com.example.capstone_design.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/friends")
public class FriendController {

    private final FriendService friendService;
    private final UserAccountRepository userAccountRepository;

    private Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userAccountRepository.findByEmail(email)
                .orElseThrow()
                .getId();
    }

    /**
     * 친구 요청 보내기
     */
    @PostMapping("/request")
    public ResponseEntity<FriendResponseDTO> sendRequest(
            @RequestBody FriendRequestDTO request
    ) {
        Long myId = getCurrentUserId();
        FriendResponseDTO res = friendService.sendRequest(myId, request.getTargetNickname());
        return ResponseEntity.ok(res);
    }

    /**
     * 내가 받은 친구 요청 목록
     */
    @GetMapping("/received")
    public ResponseEntity<List<FriendResponseDTO>> getReceivedRequests() {
        Long myId = getCurrentUserId();
        List<FriendResponseDTO> list = friendService.getReceivedRequests(myId);
        return ResponseEntity.ok(list);
    }

    /**
     * 친구 요청 수락
     */
    @PostMapping("/{requestId}/accept")
    public ResponseEntity<FriendResponseDTO> acceptRequest(@PathVariable Long requestId) {
        Long myId = getCurrentUserId();
        FriendResponseDTO res = friendService.acceptRequest(myId, requestId);
        return ResponseEntity.ok(res);
    }

    /**
     * 친구 요청 거절
     */
    @PostMapping("/{requestId}/reject")
    public ResponseEntity<FriendResponseDTO> rejectRequest(@PathVariable Long requestId) {
        Long myId = getCurrentUserId();
        FriendResponseDTO res = friendService.rejectRequest(myId, requestId);
        return ResponseEntity.ok(res);
    }

    /**
     * 친구 목록 조회
     */
    @GetMapping("/list")
    public ResponseEntity<List<FriendListDTO>> getMyFriends() {
        Long myId = getCurrentUserId();
        return ResponseEntity.ok(friendService.getMyFriends(myId));
    }

    /**
     * 친구 삭제
     */
    @DeleteMapping("/{friendId}")
    public ResponseEntity<String> deleteFriend(@PathVariable Long friendId) {
        Long myId = getCurrentUserId();
        friendService.deleteFriend(myId, friendId);
        return ResponseEntity.ok("Friend removed");
    }

    /**
     * 유저 검색
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserSimpleDTO>> searchUsers(
            @RequestParam String keyword
    ) {
        Long myId = getCurrentUserId();
        List<UserSimpleDTO> result = friendService.searchUsers(myId, keyword);
        return ResponseEntity.ok(result);
    }

}
