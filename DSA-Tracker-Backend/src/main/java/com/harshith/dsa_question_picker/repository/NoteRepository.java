package com.harshith.dsa_question_picker.repository;

import com.harshith.dsa_question_picker.dto.user.HeatmapCountDTO;
import com.harshith.dsa_question_picker.dto.user.LastActivityDTO;
import com.harshith.dsa_question_picker.model.Note;
import com.mongodb.client.MongoIterable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NoteRepository extends MongoRepository<Note, UUID> {
    Optional<Note> findByIdAndCreatedBy(UUID id, UUID createdBy);

    @Aggregation(pipeline = {
            "{ $match: { createdBy: ?0 } }",
            "{ $group: { _id: { $dateToString: { format: \"%Y-%m-%d\", date: \"$createdAt\" } }, count: { $sum: 1 } } }",
            "{ $sort: { _id: 1 } }"
    })
    List<HeatmapCountDTO> getNoteActivities(UUID createdBy);

    @Aggregation(pipeline = {
            "{ $match: { createdBy: ?0 } }",
            "{ $group: { _id: null, lastActivity: { $max: \"$createdAt\" } } }"
    })
    Optional<LastActivityDTO> getLastNoteActivity(UUID createdBy);
}
