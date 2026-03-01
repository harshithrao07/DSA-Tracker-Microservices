package com.harshith.dsa_question_picker.dto.question;

import jakarta.validation.constraints.NotBlank;

public record AutofillRequestDTO(
        @NotBlank(message = "Question link cannot be empty for autofill feature")
        String link
) {
}
