package com.resumescreening.dto;

public record ResumeFileDTO(
        byte[] data,
        String fileName,
        String fileType,
        Long fileSize
) {
}
