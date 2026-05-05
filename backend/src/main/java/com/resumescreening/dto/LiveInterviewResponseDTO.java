package com.resumescreening.dto;

import java.time.LocalDateTime;

public record LiveInterviewResponseDTO(
        Long id,
        Long candidateId,
        Long roleId,
        String roomName,
        String hostToken,
        String candidateToken,
        String hostLink,
        String candidateLink,
        String status,
        LocalDateTime createdAt
) {
}
