package com.resumescreening.dto;

import java.time.LocalDateTime;

public record HiringDecisionDTO(
        Long id,
        Long candidateId,
        Long resumeId,
        Long reportId,
        String candidateName,
        String roleName,
        String decision,
        String notes,
        LocalDateTime createdAt
) {
}
