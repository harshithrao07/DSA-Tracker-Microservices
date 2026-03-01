package com.harshith.dsa_question_picker.dto;

public record ApiResponseDTO<T>(
        boolean success,
        String errorMessage,
        T data
) {
}
