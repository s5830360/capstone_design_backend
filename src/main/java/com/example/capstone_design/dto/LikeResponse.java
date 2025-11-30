package com.example.capstone_design.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LikeResponse {
    private Long recordingId;
    private boolean liked;
    private int likeCount;
}