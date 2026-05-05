package com.resumescreening.dto;

import java.util.List;

public record CandidateRankingDTO(
        Long reportId,
        Long resumeId,
        String candidateName,
        String email,
        String roleName,
        List<String> skills,
        double experienceYears,
        double score,
        double atsScore,
        Double interviewScore,
        double finalScore,
        String status,
        String remarks,
        String hiringDecision
) {
}
