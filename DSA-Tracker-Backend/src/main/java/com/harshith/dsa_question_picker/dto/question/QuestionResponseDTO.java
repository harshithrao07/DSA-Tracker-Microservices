package com.harshith.dsa_question_picker.dto.question;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.harshith.dsa_question_picker.model.Difficulty;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record QuestionResponseDTO(
        UUID id,
        String link,
        String title,
        Difficulty difficulty,
        boolean solved,
        boolean reviseLater,
        List<String> topics,
        UUID noteId,
        Instant createdAt,
        List<Instant> solveHistory
) {
}
