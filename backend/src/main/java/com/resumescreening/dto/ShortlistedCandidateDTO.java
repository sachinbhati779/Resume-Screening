package com.resumescreening.dto;

import java.time.LocalDateTime;

public record ShortlistedCandidateDTO(
        Long id,
        Long resumeId,
        Long reportId,
        String candidateName,
        String email,
        String roleName,
        double score,
        LocalDateTime createdAt
) {
}
