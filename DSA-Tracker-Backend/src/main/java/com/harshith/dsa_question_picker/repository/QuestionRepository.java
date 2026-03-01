package com.harshith.dsa_question_picker.repository;

import com.harshith.dsa_question_picker.dto.user.HeatmapCountDTO;
import com.harshith.dsa_question_picker.dto.user.LastActivityDTO;
import com.harshith.dsa_question_picker.model.Difficulty;
import com.harshith.dsa_question_picker.model.Question;
import com.mongodb.client.MongoIterable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuestionRepository extends MongoRepository<Question, UUID> {
    Optional<Question> findByIdAndCreatedBy(UUID id, UUID createdBy);

    boolean existsByTitleAndCreatedBy(String title, UUID createdBy);

    boolean existsByIdAndCreatedBy(UUID id, UUID createdBy);

    long countByCreatedBy(UUID createdBy);

    long countByCreatedByAndSolved(UUID createdBy, boolean solved);

    long countByCreatedByAndReviseLater(UUID createdBy, boolean reviseLater);

    @Aggregation(pipeline = {
            "{ $match: { createdBy: ?0 } }",
            "{ $project: { allDates: { $concatArrays: [ [\"$createdAt\"], { $ifNull: [\"$solveHistory\", []] } ] } } }",
            "{ $unwind: \"$allDates\" }",
            "{ $group: { _id: { $dateToString: { format: \"%Y-%m-%d\", date: \"$allDates\" } }, count: { $sum: 1 } } }",
            "{ $sort: { _id: 1 } }"
    })
    List<HeatmapCountDTO> getQuestionActivities(UUID createdBy);

    @Aggregation(pipeline = {
            "{ $match: { createdBy: ?0 } }",
            "{ $project: { allDates: { $concatArrays: [ [\"$createdAt\"], { $ifNull: [\"$solveHistory\", []] } ] } } }",
            "{ $unwind: \"$allDates\" }",
            "{ $group: { _id: null, lastActivity: { $max: \"$allDates\" } } }"
    })
    Optional<LastActivityDTO> getLastQuestionActivity(UUID createdBy);
}
