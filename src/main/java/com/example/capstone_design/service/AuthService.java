package com.example.capstone_design.service;

import com.example.capstone_design.entity.UserAccount;
import com.example.capstone_design.repository.UserAccountRepository;
import com.example.capstone_design.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserAccountRepository repo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;

    // 🔹 회원가입 (email 기반)
    @Transactional
    public void signup(String email, String name, String nickname, String rawPassword) {

        if (repo.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 등록된 이메일입니다.");
        }

        if (repo.existsByNickname(nickname)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다.");
        }

        UserAccount user = UserAccount.builder()
                .email(email)
                .name(name)
                .nickname(nickname)
                .passwordHash(encoder.encode(rawPassword))
                .role("USER")
                .enabled(true)
                .build();

        repo.save(user);
    }

    // 🔹 로그인 (email 기반)
    public String login(String email, String rawPassword) {

        UserAccount user = repo.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "잘못된 이메일 또는 비밀번호"));

        if (!encoder.matches(rawPassword, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "잘못된 이메일 또는 비밀번호");
        }

        // JWT subject = email
        return jwt.generateToken(user.getEmail(), user.getRole());
    }
}
