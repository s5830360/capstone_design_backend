package com.example.capstone_design.controller;

import com.example.capstone_design.dto.DominantEmotionDTO;
import com.example.capstone_design.service.StatsService;
import com.example.capstone_design.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;
    private final UserAccountRepository userAccountRepository;

    private Long getCurrentUserId() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userAccountRepository.findByEmail(email)
                .orElseThrow()
                .getId();
    }

    @GetMapping("/calendar")
    public ResponseEntity<List<DominantEmotionDTO>> getCalendarStats(
            @RequestParam int year,
            @RequestParam int month
    ) {
        Long userId = getCurrentUserId();
        List<DominantEmotionDTO> result = statsService.getMonthlyDominantEmotions(userId, year, month);
        return ResponseEntity.ok(result);
    }
}
