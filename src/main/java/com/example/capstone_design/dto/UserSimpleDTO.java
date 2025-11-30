package com.example.capstone_design.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserSimpleDTO {
    private Long id;        // 검색된 사용자 ID
    private String nickname; // 검색된 사용자 닉네임
}
