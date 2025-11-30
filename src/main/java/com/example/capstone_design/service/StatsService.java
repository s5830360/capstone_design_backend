package com.example.capstone_design.service;

import com.example.capstone_design.dto.DominantEmotionDTO;
import com.example.capstone_design.entity.Recording;
import com.example.capstone_design.repository.RecordingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final RecordingRepository recordingRepository;

    public List<DominantEmotionDTO> getMonthlyDominantEmotions(Long userId, int year, int month) {

        // 1) 해당 월의 시작일과 마지막일
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth().plusDays(1); // createdAt < end

        // 2) 사용자 녹음 전체 가져오기
        List<Recording> list = recordingRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                userId,
                start.atStartOfDay(),
                end.atStartOfDay()
        );

        // 3) 날짜별로 그룹핑
        Map<LocalDate, List<Recording>> grouped = list.stream()
                .collect(Collectors.groupingBy(r -> r.getCreatedAt().toLocalDate()));

        List<DominantEmotionDTO> result = new ArrayList<>();

        for (LocalDate date : grouped.keySet()) {

            // 날짜별 감정 카운트
            Map<String, Long> countMap = grouped.get(date).stream()
                    .collect(Collectors.groupingBy(Recording::getEmotion, Collectors.counting()));

            // 최빈값 감정 선택
            String dominantEmotion = Collections.max(countMap.entrySet(),
                    Map.Entry.comparingByValue()).getKey();

            result.add(new DominantEmotionDTO(date.toString(), dominantEmotion));
        }

        // 날짜순으로 정렬
        result.sort(Comparator.comparing(DominantEmotionDTO::getDate));

        return result;
    }
}
