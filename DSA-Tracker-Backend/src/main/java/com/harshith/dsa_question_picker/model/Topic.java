package com.harshith.dsa_question_picker.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Data
@Builder
@Document(collection = "topics")
public class Topic {
    @Version
    private Long version;

    @Id
    private UUID id;
    private String name;
    private UUID createdBy;
}
