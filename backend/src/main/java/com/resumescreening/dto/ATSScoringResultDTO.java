package com.resumescreening.dto;

import java.util.List;

public record ATSScoringResultDTO(
        double score,
        String status,
        String remarks,
        List<String> matchedKeywords,
        List<String> missingKeywords,
        ATSScoreBreakdownDTO scoreBreakdown,
        ATSChecksDTO atsChecks,
        String explanation
) {
}
