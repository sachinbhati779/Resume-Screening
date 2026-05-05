package com.resumescreening.dto;

import java.time.LocalDateTime;

public record RecentActivityDTO(
        String type,
        String title,
        String description,
        LocalDateTime createdAt
) {
}
