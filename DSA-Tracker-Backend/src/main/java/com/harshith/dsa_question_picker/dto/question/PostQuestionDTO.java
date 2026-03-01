package com.harshith.dsa_question_picker.dto.question;

import com.harshith.dsa_question_picker.model.Difficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PostQuestionDTO(
        @NotBlank(message = "Problem Title cannot be empty")
        String title,

        @NotBlank(message = "Problem Link cannot be empty")
        String link,

        boolean reviseLater,

        @NotEmpty(message = "At least one topic must be provided")
        List<@NotBlank(message = "Topic ID cannot be blank") String> topics,

        @NotNull(message = "Problem Difficulty must be provided")
        Difficulty difficulty,

        String note
) {
}
