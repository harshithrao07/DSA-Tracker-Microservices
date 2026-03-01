package com.harshith.dsa_question_picker.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "questions")
public class Question {
    @Version
    private Long version;

    @Id
    private UUID id;
    private String title;
    private String link;
    private Difficulty difficulty;
    private boolean solved;
    private boolean reviseLater;
    @JsonProperty("topics")
    private List<UUID> topicIds = new ArrayList<>();
    private UUID noteId;
    private UUID createdBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @CreatedDate
    private Instant createdAt;

    private List<Instant> solveHistory = new ArrayList<>();
}
