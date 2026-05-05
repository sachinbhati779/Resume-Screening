package com.resumescreening.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ScreeningRequestDTO(
        @NotNull Long roleId,
        List<Long> resumeIds
) {
}
