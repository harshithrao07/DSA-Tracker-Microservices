package com.harshith.dsa_question_picker.dto.question;

import java.time.Instant;
import java.util.List;

public record QuestionTimelineDTO(
        Instant createdAt,
        List<String> updateHistory
) {
}
