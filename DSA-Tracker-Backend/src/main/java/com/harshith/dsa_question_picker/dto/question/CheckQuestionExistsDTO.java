package com.harshith.dsa_question_picker.dto.question;

import jakarta.validation.constraints.NotBlank;

public record CheckQuestionExistsDTO(
        @NotBlank(message = "Problem title cannot be empty")
        String title
) {
}
