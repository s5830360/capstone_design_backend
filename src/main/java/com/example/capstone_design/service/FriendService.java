package com.example.capstone_design.service;

import com.example.capstone_design.dto.FriendListDTO;
import com.example.capstone_design.dto.FriendRequestDTO;
import com.example.capstone_design.dto.FriendResponseDTO;
import com.example.capstone_design.dto.UserSimpleDTO;
import com.example.capstone_design.entity.Friend;
import com.example.capstone_design.entity.FriendStatus;
import com.example.capstone_design.entity.NotificationType;
import com.example.capstone_design.entity.UserAccount;
import com.example.capstone_design.repository.FriendRepository;
import com.example.capstone_design.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserAccountRepository userAccountRepository;
    private final NotificationService notificationService;

    /**
     * 친구 요청 보내기
     */
    @Transactional
    public FriendResponseDTO sendRequest(Long myId, String targetNickname) {

        UserAccount me = userAccountRepository.findById(myId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        UserAccount target = userAccountRepository.findByNickname(targetNickname)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Target user not found"));

        if (me.getId().equals(target.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot send request to yourself");
        }

        // 이미 친구 요청 또는 친구 상태인지 확인
        friendRepository.findByUserIdAndFriendId(me.getId(), target.getId()).ifPresent(f -> {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Already requested or already friend");
        });

        friendRepository.findByUserIdAndFriendId(target.getId(), me.getId()).ifPresent(f -> {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target already sent you a request");
        });

        Friend f = Friend.builder()
                .user(me)
                .friend(target)
                .status(FriendStatus.PENDING)
                .build();

        friendRepository.save(f);

        notificationService.push(
                target.getId(),
                NotificationType.FRIEND_REQUEST,
                me.getNickname() + "님이 친구 요청을 보냈습니다.",
                null,
                me.getId()
        );


        return FriendResponseDTO.of(f);
    }


    /**
     * 받은 친구 요청 목록
     */
    @Transactional(readOnly = true)
    public List<FriendResponseDTO> getReceivedRequests(Long myId) {
        List<Friend> list = friendRepository.findAllByFriendIdAndStatus(myId, FriendStatus.PENDING);
        return list.stream().map(FriendResponseDTO::of).toList();
    }


    /**
     * 친구 요청 수락
     */
    @Transactional
    public FriendResponseDTO acceptRequest(Long myId, Long requestId) {

        Friend f = friendRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend request not found"));

        if (!f.getFriend().getId().equals(myId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your request");
        }

        f.setStatus(FriendStatus.ACCEPTED);
        friendRepository.save(f);

        notificationService.push(
                f.getUser().getId(),                        // 알림 받을 사람 (요청 보낸 사람)
                NotificationType.FRIEND_ACCEPT,             // 알림 타입
                f.getFriend().getNickname() + "님이 친구 요청을 수락했습니다.",  // 메시지
                null,                                       // recordingId 없음
                f.getFriend().getId()                       // 알림 보낸 사람 = 수락한 사람
        );

        return FriendResponseDTO.of(f);
    }

    /**
     * 친구 요청 거절
     */
    @Transactional
    public FriendResponseDTO rejectRequest(Long myId, Long requestId) {

        Friend friend = friendRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));

        // 받은 요청인지 검증
        if (!friend.getFriend().getId().equals(myId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your friend request");
        }

        // 거절 → row 삭제
        friendRepository.delete(friend);

        return new FriendResponseDTO(
                requestId,
                friend.getUser().getId(),
                friend.getUser().getNickname(),
                myId,
                friend.getFriend().getNickname(),
                FriendStatus.REJECTED
        );
    }


    /**
     * 친구 목록 조회 (양방향 ACCEPTED 다 포함)
     */
    @Transactional(readOnly = true)
    public List<FriendListDTO> getMyFriends(Long myId) {
        List<Friend> friends = friendRepository.findByUserIdOrFriendIdAndStatus(myId, myId, FriendStatus.ACCEPTED);

        return friends.stream()
                .map(f -> {
                    UserAccount friendUser;

                    if (f.getUser().getId().equals(myId)) {
                        friendUser = f.getFriend();
                    } else {
                        friendUser = f.getUser();
                    }

                    return new FriendListDTO(friendUser.getId(), friendUser.getNickname());
                })
                .toList();
    }

    /**
     * 친구 삭제
     */
    @Transactional
    public void deleteFriend(Long myId, Long friendId) {

        // 양쪽 방향의 ACCEPTED 관계 제거
        Friend relation1 = friendRepository
                .findByUserIdAndFriendId(myId, friendId)
                .orElse(null);

        Friend relation2 = friendRepository
                .findByUserIdAndFriendId(friendId, myId)
                .orElse(null);

        if (relation1 == null && relation2 == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not friends");
        }

        if (relation1 != null) friendRepository.delete(relation1);
        if (relation2 != null) friendRepository.delete(relation2);
    }

    /**
     * 친구 userId 목록
     */
    @Transactional(readOnly = true)
    public List<Long> getMyFriendIds(Long myId) {

        List<Friend> friends = friendRepository
                .findByUserIdOrFriendIdAndStatus(myId, myId, FriendStatus.ACCEPTED);

        List<Long> ids = new ArrayList<>();

        for (Friend f : friends) {
            if (f.getUser().getId().equals(myId)) {
                ids.add(f.getFriend().getId());   // 내가 요청 → 친구
            } else {
                ids.add(f.getUser().getId());     // 상대가 요청 → 나 친구
            }
        }

        return ids;
    }


    /**
     * 특정 유저의 피드만 조회하기 위한 친구 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean isFriend(Long myId, Long targetId) {
        return friendRepository.findByUserIdAndFriendId(myId, targetId)
                .filter(f -> f.getStatus() == FriendStatus.ACCEPTED)
                .isPresent()
                ||
                friendRepository.findByUserIdAndFriendId(targetId, myId)
                        .filter(f -> f.getStatus() == FriendStatus.ACCEPTED)
                        .isPresent();
    }

    /**
     * 유저 검색
     */
    @Transactional(readOnly = true)
    public List<UserSimpleDTO> searchUsers(Long myId, String keyword) {

        // keyword 포함한 모든 사용자 검색
        List<UserAccount> users = userAccountRepository.findByNicknameContaining(keyword);

        return users.stream()
                .filter(u -> !u.getId().equals(myId)) // 🔥 자기 자신 제외
                .map(u -> new UserSimpleDTO(u.getId(), u.getNickname()))
                .toList();
    }

}