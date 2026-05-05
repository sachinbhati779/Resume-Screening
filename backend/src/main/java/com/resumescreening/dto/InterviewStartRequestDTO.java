package com.resumescreening.dto;

import jakarta.validation.constraints.NotNull;

public record InterviewStartRequestDTO(
        @NotNull Long candidateId,
        @NotNull Long roleId
) {
}
