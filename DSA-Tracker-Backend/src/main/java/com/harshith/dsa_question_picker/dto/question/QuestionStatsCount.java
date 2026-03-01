package com.harshith.dsa_question_picker.dto.question;

import java.util.List;

public record QuestionStatsCount(
        long totalQuestions,
        long solvedQuestions,
        long remQuestions,
        long markedForRevision,
        List<QuestionStatsCountDifficulty> questionStatsCountDifficulties,
        List<QuestionStatsCountTopic> questionStatsCountTopics
) {
}
