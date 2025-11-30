package com.example.capstone_design.dto;

import com.example.capstone_design.entity.Friend;
import com.example.capstone_design.entity.FriendStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FriendResponseDTO {

    private Long id;
    private Long userId;         // 요청 보낸 사람
    private String userNickname;

    private Long friendId;       // 받는 사람
    private String friendNickname;

    private FriendStatus status; // PENDING / ACCEPTED

    public static FriendResponseDTO of(Friend f) {
        return new FriendResponseDTO(
                f.getId(),
                f.getUser().getId(),
                f.getUser().getNickname(),
                f.getFriend().getId(),
                f.getFriend().getNickname(),
                f.getStatus()
        );
    }
}
