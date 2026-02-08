package com.harshith.autofill_service.dto;

import java.util.List;

public record QuestionAutofillDTO(
        String platform,
        String title,
        List<String> topics,
        Difficulty difficulty
) {
}
