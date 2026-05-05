package com.resumescreening.dto;

import jakarta.validation.constraints.NotNull;

public record LiveInterviewCreateRequestDTO(
        @NotNull Long candidateId,
        @NotNull Long roleId
) {
}
