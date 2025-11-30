package com.example.capstone_design.controller;

import com.example.capstone_design.dto.UserResponse;
import com.example.capstone_design.entity.UserAccount;
import com.example.capstone_design.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 🔹 내 정보 조회 (email 기반)
    @GetMapping("/me")
    public UserResponse me() {

        // JWT subject = email
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        UserAccount u = userService.findByEmail(email);

        return new UserResponse(
                u.getId(),
                u.getEmail(),
                u.getName(),
                u.getNickname(),
                u.getRole(),
                u.isEnabled(),
                u.getCreatedAt()
        );
    }

    // 🔹 (선택) 관리자만 전체 목록 보기
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UserResponse> listForAdmin() {
        return userService.list().stream()
                .map(u -> new UserResponse(
                        u.getId(),
                        u.getEmail(),
                        u.getName(),
                        u.getNickname(),
                        u.getRole(),
                        u.isEnabled(),
                        u.getCreatedAt()
                ))
                .toList();
    }

    // 🔹 관리자용 유저 삭제
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void adminDelete(@PathVariable Long id) {
        userService.delete(id);
    }
}
