package com.resumescreening.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ScreeningResponseDTO(
        Long reportId,
        Long resumeId,
        Long roleId,
        String candidateName,
        String roleName,
        double score,
        String status,
        String remarks,
        List<String> matchedKeywords,
        List<String> missingKeywords,
        ATSScoreBreakdownDTO scoreBreakdown,
        ATSChecksDTO atsChecks,
        String explanation,
        LocalDateTime createdAt
) {
}
