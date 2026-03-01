package com.harshith.dsa_question_picker.dto.topic;

import jakarta.validation.constraints.NotBlank;

public record PostTopicDTO(
        @NotBlank(message = "Topic name cannot be empty")
        String name
) {
}
