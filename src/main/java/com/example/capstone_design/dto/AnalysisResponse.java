package com.example.capstone_design.dto;

import lombok.Data;

@Data
public class AnalysisResponse {

    // 최종 출력값 (Spring이 저장할 값)
    private String final_emotion;
    private Double final_confidence;

    // 보조 정보(프론트에서 원하면 쓸 수 있음)
    private Boolean voice_detected;
    private String voice_emotion;
    private Double voice_confidence;

    private String scene;
    private String scene_emotion;
    private Double scene_confidence;

}
