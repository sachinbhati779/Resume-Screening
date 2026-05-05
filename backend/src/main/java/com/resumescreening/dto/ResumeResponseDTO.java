package com.resumescreening.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ResumeResponseDTO(
        Long id,
        String candidateName,
        String email,
        String phone,
        List<String> skills,
        double experienceYears,
        String education,
        List<String> projects,
        String summary,
        String appliedRole,
        String fileName,
        String fileType,
        Long fileSize,
        LocalDateTime createdAt
) {
}
