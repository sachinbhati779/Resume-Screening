package com.resumescreening.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record ResumeRequestDTO(
        @NotBlank String candidateName,
        @Email @NotBlank String email,
        String phone,
        @NotEmpty List<String> skills,
        @Min(0) double experienceYears,
        @NotBlank String education,
        List<String> projects,
        @NotBlank String summary,
        @NotBlank String appliedRole
) {
}
