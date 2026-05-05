package com.resumescreening.dto;

import java.time.LocalDateTime;
import java.util.List;

public record JobRoleResponseDTO(
        Long id,
        String roleName,
        List<String> requiredSkills,
        double minExperience,
        String requiredEducation,
        List<String> keywords,
        int skillWeightage,
        int experienceWeightage,
        int projectWeightage,
        int educationWeightage,
        int keywordWeightage,
        LocalDateTime createdAt
) {
}
