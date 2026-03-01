package com.harshith.dsa_question_picker.service;

import com.harshith.dsa_question_picker.dto.ApiResponseDTO;
import com.harshith.dsa_question_picker.dto.user.HeatmapActivityDTO;
import com.harshith.dsa_question_picker.dto.question.QuestionStatsCount;
import com.harshith.dsa_question_picker.dto.question.QuestionStatsCountDifficulty;
import com.harshith.dsa_question_picker.dto.question.QuestionStatsCountTopic;
import com.harshith.dsa_question_picker.dto.user.HeatmapCountDTO;
import com.harshith.dsa_question_picker.model.Question;
import com.harshith.dsa_question_picker.repository.NoteRepository;
import com.harshith.dsa_question_picker.repository.QuestionRepository;
import com.mongodb.BasicDBObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {
    private final QuestionRepository questionRepository;
    private final NoteRepository noteRepository;
    private final MongoTemplate mongoTemplate;

    public ResponseEntity<ApiResponseDTO<QuestionStatsCount>> getQuestionStatsCount(UUID createdBy) {
        try {
            Criteria baseCriteria = Criteria.where("createdBy").is(createdBy);

            long totalQuestions = questionRepository.countByCreatedBy(createdBy);
            long solvedQuestions = questionRepository.countByCreatedByAndSolved(createdBy, true);
            long remQuestions = questionRepository.countByCreatedByAndSolved(createdBy, false);
            long markedForRevision = questionRepository.countByCreatedByAndReviseLater(createdBy, true);

            Aggregation aggBasedOnDifficulty = Aggregation.newAggregation(
                    Aggregation.match(baseCriteria),
                    Aggregation.group("difficulty")
                            .count().as("totalQuestions")
                            .sum(ConditionalOperators.when(Criteria.where("solved").is(true)).then(1).otherwise(0)).as("solvedQuestions")
                            .sum(ConditionalOperators.when(Criteria.where("solved").is(false)).then(1).otherwise(0)).as("remQuestions"),
                    Aggregation.project("totalQuestions", "solvedQuestions", "remQuestions")
                            .and("_id").as("name")
            );

            AggregationResults<QuestionStatsCountDifficulty> resultsBasedOnDifficulty =
                    mongoTemplate.aggregate(aggBasedOnDifficulty, "questions", QuestionStatsCountDifficulty.class);

            List<QuestionStatsCountDifficulty> questionStatsCountDifficulties = resultsBasedOnDifficulty.getMappedResults();

            Aggregation aggBasedOnTopic = Aggregation.newAggregation(
                    Aggregation.match(baseCriteria),
                    Aggregation.unwind("topicIds"),
                    Aggregation.group("topicIds", "difficulty")
                            .count().as("totalQuestions")
                            .sum(ConditionalOperators.when(Criteria.where("solved").is(true)).then(1).otherwise(0)).as("solvedQuestions")
                            .sum(ConditionalOperators.when(Criteria.where("solved").is(false)).then(1).otherwise(0)).as("remQuestions"),
                    Aggregation.group("_id.topicIds")
                            .sum("totalQuestions").as("totalQuestions")
                            .sum("solvedQuestions").as("solvedQuestions")
                            .sum("remQuestions").as("remQuestions")
                            .push(
                                    new BasicDBObject("name", "$_id.difficulty")
                                            .append("totalQuestions", "$totalQuestions")
                                            .append("solvedQuestions", "$solvedQuestions")
                                            .append("remQuestions", "$remQuestions")
                            ).as("questionStatsCountDifficulties"),
                    Aggregation.lookup("topics", "_id", "_id", "topicInfo"),
                    Aggregation.unwind("topicInfo", true),
                    Aggregation.project("totalQuestions", "solvedQuestions", "remQuestions", "questionStatsCountDifficulties")
                            .and("_id").as("id")
                            .and("topicInfo.name").as("name")
            );

            AggregationResults<QuestionStatsCountTopic> resultsBasedOnTopic =
                    mongoTemplate.aggregate(aggBasedOnTopic, "questions", QuestionStatsCountTopic.class);

            List<QuestionStatsCountTopic> questionStatsCountTopics = resultsBasedOnTopic.getMappedResults();

            QuestionStatsCount questionStatsCount = new QuestionStatsCount(
                    totalQuestions,
                    solvedQuestions,
                    remQuestions,
                    markedForRevision,
                    questionStatsCountDifficulties,
                    questionStatsCountTopics
            );

            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(true, null, questionStatsCount));
        } catch (Exception e) {
            log.error("An exception has occurred {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponseDTO<>(false, "An error occurred in the server", null));
        }
    }

    public ResponseEntity<ApiResponseDTO<Boolean>> resetProgress(UUID createdBy) {
        try {
            Query query = new Query(Criteria.where("createdBy").is(createdBy));

            Update update = new Update().set("solved", false);

            mongoTemplate.updateMulti(query, update, Question.class);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ApiResponseDTO<>(true, null, true));
        } catch (Exception e) {
            log.error("An exception has occurred {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO<>(false, "An error occurred in the server", false));
        }
    }

    public ResponseEntity<ApiResponseDTO<List<HeatmapActivityDTO>>> getHeatmapActivities(UUID createdBy) {
        try {
            List<HeatmapCountDTO> questionResults = questionRepository.getQuestionActivities(createdBy);
            List<HeatmapCountDTO> noteResults = noteRepository.getNoteActivities(createdBy);

            Map<String, Long> merged = new HashMap<>();

            Stream.concat(questionResults.stream(), noteResults.stream())
                    .forEach(res -> merged.merge(res._id(), res.count(), Long::sum));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");

            List<HeatmapActivityDTO> heatmapActivityDTOS = merged.entrySet().stream()
                    .map(e -> new HeatmapActivityDTO(
                            LocalDate.parse(e.getKey())
                                    .format(formatter),     
                            e.getValue()
                    ))
                    .sorted(Comparator.comparing(HeatmapActivityDTO::date))
                    .toList();

            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(true, null, heatmapActivityDTOS));
        } catch (Exception e) {
            log.error("An exception has occurred {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO<>(false, "An error occurred in the server", null));
        }
    }
}
