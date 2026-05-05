package com.resumescreening.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record HiringDecisionRequestDTO(
        Long candidateId,
        @NotNull Long resumeId,
        @NotNull Long reportId,
        @NotBlank String decision,
        String notes
) {
}
