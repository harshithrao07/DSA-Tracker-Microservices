package com.harshith.dsa_question_picker.dto.question;

import com.harshith.dsa_question_picker.model.Difficulty;

import java.util.List;

public record QuestionAutofillDTO(
        String platform,
        String title,
        List<String> topics,
        Difficulty difficulty
) {
}
