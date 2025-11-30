package com.example.capstone_design.controller;

import com.example.capstone_design.dto.LikeResponse;
import com.example.capstone_design.repository.UserAccountRepository;
import com.example.capstone_design.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recordings")
public class LikeController {

    private final LikeService likeService;
    private final UserAccountRepository userAccountRepository;

    @PostMapping("/{id}/like")
    public ResponseEntity<LikeResponse> like(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = userAccountRepository.findByEmail(email)
                .orElseThrow()
                .getId();

        LikeResponse response = likeService.like(id, userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<LikeResponse> unlike(@PathVariable Long id) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Long userId = userAccountRepository.findByEmail(email)
                .orElseThrow()
                .getId();

        LikeResponse response = likeService.unlike(id, userId);
        return ResponseEntity.ok(response);
    }
}
