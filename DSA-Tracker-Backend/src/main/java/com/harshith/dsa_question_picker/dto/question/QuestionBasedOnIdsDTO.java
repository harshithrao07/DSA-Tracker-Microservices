package com.harshith.dsa_question_picker.dto.question;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record QuestionBasedOnIdsDTO(
        @NotEmpty(message = "At least one question id must be provided")
        List<UUID> questionsIds
) {
}
