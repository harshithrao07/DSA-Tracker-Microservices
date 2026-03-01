package com.harshith.dsa_question_picker.dto.topic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TopicResponseDTO(
        UUID id,
        String name
) {
}
