package com.resumescreening.dto;

import java.util.List;

public record ATSChecksDTO(
        boolean complete,
        boolean readable,
        boolean simpleFormatting,
        boolean hasRequiredSections,
        List<String> issues
) {
}
