package com.resumescreening.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponseDTO<T>(
        boolean success,
        String message,
        T data,
        String errorCode
) {
    public static <T> ApiResponseDTO<T> success(String message, T data) {
        return new ApiResponseDTO<>(true, message, data, null);
    }

    public static ApiResponseDTO<Void> success(String message) {
        return new ApiResponseDTO<>(true, message, null, null);
    }

    public static ApiResponseDTO<Void> error(String message, String errorCode) {
        return new ApiResponseDTO<>(false, message, null, errorCode);
    }
}
