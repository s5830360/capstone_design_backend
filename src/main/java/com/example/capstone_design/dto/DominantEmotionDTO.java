package com.example.capstone_design.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DominantEmotionDTO {
    private String date;            // yyyy-MM-dd
    private String dominantEmotion; // 가장 많이 기록된 감정
}
