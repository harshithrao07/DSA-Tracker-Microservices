package com.harshith.dsa_question_picker.dto.note;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PostNoteDTO(
        @NotNull(message = "Question Id cannot be null")
        UUID questionId,

        @NotBlank(message = "Notes cannot be empty")
        String text
) {
}
