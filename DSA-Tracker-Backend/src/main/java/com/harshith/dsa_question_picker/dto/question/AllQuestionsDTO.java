package com.harshith.dsa_question_picker.dto.question;

import java.util.List;

public record AllQuestionsDTO(
        long totalQuestions,
        long solvedQuestions,
        long remQuestions,
        List<QuestionResponseDTO> questionResponseDTOList
) {
}
