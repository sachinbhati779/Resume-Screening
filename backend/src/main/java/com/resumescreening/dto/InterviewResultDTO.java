package com.resumescreening.dto;

import java.time.LocalDateTime;

public record InterviewResultDTO(
        Long sessionId,
        double finalScore,
        String recommendation,
        String strengths,
        String weaknesses,
        LocalDateTime createdAt
) {
}
