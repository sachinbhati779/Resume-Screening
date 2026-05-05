package com.resumescreening.dto;

public record LiveInterviewAccessDTO(
        Long id,
        String roomName,
        String jitsiUrl,
        String candidateName,
        String roleName,
        boolean host,
        String status
) {
}
