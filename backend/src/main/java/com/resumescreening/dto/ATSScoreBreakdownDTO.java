package com.resumescreening.dto;

public record ATSScoreBreakdownDTO(
        double skillsScore,
        double experienceScore,
        double projectScore,
        double educationScore,
        double keywordScore
) {
}
