package com.resumescreening.dto;

public record DashboardSummaryDTO(
        long totalResumes,
        long shortlistedCandidates,
        long rejectedCandidates,
        double averageScore
) {
}
