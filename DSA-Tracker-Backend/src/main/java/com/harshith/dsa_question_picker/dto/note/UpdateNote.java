package com.harshith.dsa_question_picker.dto.note;

import jakarta.validation.constraints.NotNull;

public record UpdateNote(
        @NotNull
        String text
) {
}
