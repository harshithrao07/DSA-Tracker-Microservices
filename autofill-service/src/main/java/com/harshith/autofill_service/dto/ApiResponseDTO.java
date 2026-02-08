package com.harshith.autofill_service.dto;

public record ApiResponseDTO<T>(
        boolean success,
        String errorMessage,
        T data
) {
}
