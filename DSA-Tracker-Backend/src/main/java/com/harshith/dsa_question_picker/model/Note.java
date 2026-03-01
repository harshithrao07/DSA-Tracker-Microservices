package com.harshith.dsa_question_picker.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@Document(collection = "notes")
public class Note {
    @Version
    private Long version;

    @Id
    private UUID id;
    private UUID questionId;
    private String text;
    private UUID createdBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @CreatedDate
    private Instant createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @LastModifiedDate
    private Instant updatedAt;
}
