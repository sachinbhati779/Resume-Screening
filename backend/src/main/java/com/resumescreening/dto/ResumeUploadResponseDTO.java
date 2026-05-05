package com.resumescreening.dto;

import java.util.List;

public record ResumeUploadResponseDTO(
        int uploadedCount,
        List<ResumeResponseDTO> resumes,
        List<String> warnings
) {
}
