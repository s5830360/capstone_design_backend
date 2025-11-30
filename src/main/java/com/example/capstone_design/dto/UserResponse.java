package com.example.capstone_design.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;       // username → email로 변경
    private String name;
    private String nickname;
    private String role;
    private boolean enabled;
    private LocalDateTime createdAt;
}
