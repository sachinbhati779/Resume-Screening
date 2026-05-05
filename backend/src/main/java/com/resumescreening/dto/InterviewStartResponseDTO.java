package com.resumescreening.dto;

public record InterviewStartResponseDTO(
        Long sessionId,
        Long candidateId,
        String roleName,
        int totalQuestions,
        String status
) {
}
