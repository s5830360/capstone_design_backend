package com.example.capstone_design.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class AuthDtos {

    // ---------- 회원가입 ----------
    @Getter @Setter @NoArgsConstructor
    public static class SignupRequest {

        @Email
        @NotBlank
        private String email;      // 로그인 ID

        @NotBlank
        private String name;       // 사용자 실명 or 표시명

        @NotBlank
        private String nickname;   // 앱에서 보일 닉네임

        @NotBlank
        private String password;   // 평문 비밀번호 (서비스에서 해시 처리)
    }

    // ---------- 로그인 ----------
    @Getter @Setter @NoArgsConstructor
    public static class LoginRequest {

        @Email
        @NotBlank
        private String email;     // 이제 username 대신 email 사용

        @NotBlank
        private String password;
    }

    // ---------- 로그인 응답 (JWT) ----------
    @Getter @AllArgsConstructor
    public static class TokenResponse {
        private String token;
    }
}
