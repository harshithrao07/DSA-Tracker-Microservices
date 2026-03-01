package com.harshith.dsa_question_picker.dto.note;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NoteResponseDTO(
        UUID id,
        UUID questionId,
        String text,
        Instant createdAt,
        Instant updatedAt
) {
}
