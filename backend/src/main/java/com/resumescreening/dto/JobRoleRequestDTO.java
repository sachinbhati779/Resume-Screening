package com.resumescreening.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record JobRoleRequestDTO(
        @NotBlank String roleName,
        @NotEmpty List<String> requiredSkills,
        @Min(0) double minExperience,
        @NotBlank String requiredEducation,
        List<String> keywords,
        Integer skillWeightage,
        Integer experienceWeightage,
        Integer projectWeightage,
        Integer educationWeightage,
        Integer keywordWeightage
) {
}
