package com.example.capstone_design.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FriendListDTO {

    private Long friendId;          // 친구의 사용자 ID
    private String friendNickname;  // 친구의 닉네임

}
