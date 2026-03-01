package com.harshith.dsa_question_picker.dto.question;

import com.harshith.dsa_question_picker.model.Difficulty;

import java.util.List;
import java.util.UUID;

public record UpdateQuestionDTO(
        String title,
        String link,
        Boolean reviseLater,
        Boolean solved,
        List<UUID> topicIds,
        Difficulty difficulty
) {
}
