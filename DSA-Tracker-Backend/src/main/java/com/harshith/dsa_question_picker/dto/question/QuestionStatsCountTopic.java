package com.harshith.dsa_question_picker.dto.question;

import java.util.List;
import java.util.UUID;

public record QuestionStatsCountTopic(
        UUID id,
        String name,
        long totalQuestions,
        long solvedQuestions,
        long remQuestions,
        List<QuestionStatsCountDifficulty> questionStatsCountDifficulties
) {
}
