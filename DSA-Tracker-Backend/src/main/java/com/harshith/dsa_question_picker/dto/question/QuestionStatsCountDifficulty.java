package com.harshith.dsa_question_picker.dto.question;

public record QuestionStatsCountDifficulty(
        String name,
        long totalQuestions,
        long solvedQuestions,
        long remQuestions
) {
}
