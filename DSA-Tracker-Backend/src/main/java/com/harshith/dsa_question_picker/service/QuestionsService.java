package com.harshith.dsa_question_picker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harshith.dsa_question_picker.client.AutofillClient;
import com.harshith.dsa_question_picker.dto.ApiResponseDTO;
import com.harshith.dsa_question_picker.dto.question.*;
import com.harshith.dsa_question_picker.model.*;
import com.harshith.dsa_question_picker.repository.NoteRepository;
import com.harshith.dsa_question_picker.repository.QuestionRepository;
import com.harshith.dsa_question_picker.repository.TopicRepository;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.harshith.dsa_question_picker.utils.Utility.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class QuestionsService {
    private final QuestionRepository questionRepository;
    private final NoteRepository noteRepository;
    private final TopicRepository topicRepository;
    private final ObjectMapper objectMapper;
    private final MongoTemplate mongoTemplate;
    private final AutofillClient autofillClient;

    public ResponseEntity<ApiResponseDTO<AllQuestionsDTO>> getAllQuestion(int page, int pageSize, String key, List<String> topics, String difficulty, String status, String sortBy, String sortDir, UUID createdBy) {
        try {
            Sort sort = Sort.by(sortBy);
            sort = sortDir.equals("desc") ? sort.descending() : sort.ascending();
            Pageable pageable = PageRequest.of(page, pageSize, sort);

            Query query = new Query().with(pageable);

            if (key != null && !key.isBlank()) {
                query.addCriteria(Criteria.where("title").regex(key, "i")); // "i" is the case-insensitive flag
            }

            if (topics != null && !topics.isEmpty()) {
                List<UUID> topicIds = topics.stream()
                        .filter(topic -> topicRepository.existsByNameAndCreatedBy(topic, createdBy))
                        .map(topic -> topicRepository.findByNameAndCreatedBy(topic, createdBy))
                        .filter(Optional::isPresent)
                        .map(opt -> opt.get().getId())
                        .toList();

                if (!topicIds.isEmpty()) {
                    query.addCriteria(Criteria.where("topicIds").in(topicIds));
                }
            }

            if (difficulty != null && !difficulty.isBlank()) {
                query.addCriteria(Criteria.where("difficulty").is(Difficulty.valueOf(difficulty.toUpperCase())));
            }

            if (status != null && !status.isBlank()) {
                if (status.equals("reviseLater")) {
                    query.addCriteria(Criteria.where("reviseLater").is(true));
                }

                if (status.equals("solved")) {
                    query.addCriteria(Criteria.where("solved").is(true));
                }

                if (status.equals("notSolved")) {
                    query.addCriteria(Criteria.where("solved").is(false));
                }
            }

            query.addCriteria(Criteria.where("createdBy").is(createdBy));

            List<Question> questions = mongoTemplate.find(query, Question.class);

            Set<UUID> allTopicIds = questions.stream()
                    .flatMap(q -> q.getTopicIds().stream())
                    .collect(Collectors.toSet());

            Map<UUID, String> topicMap = topicRepository.findAllById(allTopicIds).stream()
                    .collect(Collectors.toMap(Topic::getId, Topic::getName));

            List<QuestionResponseDTO> questionResponseDTOS = questions.stream()
                    .map(question -> {
                        List<String> topicNames = question.getTopicIds().stream()
                                .map(topicMap::get) // lookup from pre-fetched map
                                .filter(Objects::nonNull)
                                .toList();

                        return new QuestionResponseDTO(
                                question.getId(),
                                question.getLink(),
                                question.getTitle(),
                                question.getDifficulty(),
                                question.isSolved(),
                                question.isReviseLater(),
                                topicNames,
                                question.getNoteId(),
                                question.getCreatedAt(),
                                question.getSolveHistory()
                        );
                    })
                    .toList();

            long totalQuestions = questionRepository.countByCreatedBy(createdBy);
            long solvedQuestions = questionRepository.countByCreatedByAndSolved(createdBy, true);
            long remQuestions = questionRepository.countByCreatedByAndSolved(createdBy, false);

            AllQuestionsDTO allQuestionsDTO = new AllQuestionsDTO(totalQuestions, solvedQuestions, remQuestions, questionResponseDTOS);

            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(true, null, allQuestionsDTO));
        } catch (Exception e) {
            log.error("An exception has occurred {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponseDTO<>(false, "An error occurred in the server", null));
        }
    }

    public ResponseEntity<ApiResponseDTO<QuestionResponseDTO>> addQuestion(@Valid PostQuestionDTO postQuestionDTO, UUID createdBy) {
        try {
            if (questionRepository.existsByTitleAndCreatedBy(postQuestionDTO.title(), createdBy)) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponseDTO<>(false, "Problem with the same title already exists", null));
            }

            if (!isValidUrl(postQuestionDTO.link())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseDTO<>(false, "Problem link is invalid", null));
            }

            Question question = Question.builder()
                    .id(UUID.randomUUID())
                    .title(postQuestionDTO.title())
                    .link(postQuestionDTO.link())
                    .difficulty(postQuestionDTO.difficulty())
                    .createdBy(createdBy)
                    .reviseLater(postQuestionDTO.reviseLater())
                    .build();

            List<UUID> topicIds = new ArrayList<>();
            for (int i = 0; i < postQuestionDTO.topics().size(); i++) {
                String topicName = postQuestionDTO.topics().get(i);
                Topic topic = topicRepository.findByNameAndCreatedBy(topicName, createdBy).orElse(null);
                if (topic == null) {
                    topic = Topic.builder()
                            .id(UUID.randomUUID())
                            .name(topicName)
                            .createdBy(createdBy)
                            .build();
                    topic = topicRepository.save(topic);
                }
                topicIds.add(topic.getId());
            }
            question.setTopicIds(topicIds);

            if (postQuestionDTO.note() != null && !postQuestionDTO.note().isBlank()) {
                Note note = Note.builder()
                        .id(UUID.randomUUID())
                        .questionId(question.getId())
                        .text(postQuestionDTO.note())
                        .createdBy(createdBy)
                        .build();

                Note savedNote = noteRepository.save(note);

                question.setNoteId(savedNote.getId());
            }

            question = questionRepository.save(question);

            QuestionResponseDTO questionResponseDTO = objectMapper.convertValue(question, QuestionResponseDTO.class);
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(true, null, questionResponseDTO));
        } catch (Exception e) {
            log.error("An exception has occurred {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponseDTO<>(false, "An error occurred in the server", null));
        }
    }

    public ResponseEntity<ApiResponseDTO<QuestionResponseDTO>> updateQuestion(@Valid UpdateQuestionDTO updateQuestionDTO, String questionId, UUID createdBy) {
        try {
            Optional<Question> question = questionRepository.findByIdAndCreatedBy(UUID.fromString(questionId), createdBy);
            if (question.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseDTO<>(false, "Question with id:" + questionId + " does not exist", null));
            }

            if (updateQuestionDTO.title() != null && !updateQuestionDTO.title().isBlank()) {
                question.get().setTitle(updateQuestionDTO.title());
            }

            if (updateQuestionDTO.link() != null && !updateQuestionDTO.link().isBlank()) {
                if (!isValidUrl(updateQuestionDTO.link())) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseDTO<>(false, "Problem link is invalid", null));
                }

                question.get().setLink(updateQuestionDTO.link());
            }

            if (updateQuestionDTO.reviseLater() != null) {
                question.get().setReviseLater(updateQuestionDTO.reviseLater());
            }

            if (updateQuestionDTO.solved() != null) {
                question.get().setSolved(updateQuestionDTO.solved());

                if (updateQuestionDTO.solved()) {
                    question.get().getSolveHistory().add(Instant.now());
                }
            }

            if (updateQuestionDTO.difficulty() != null) {
                question.get().setDifficulty(updateQuestionDTO.difficulty());
            }

            if (updateQuestionDTO.topicIds() != null && !updateQuestionDTO.topicIds().isEmpty()) {
                for (UUID topicId : updateQuestionDTO.topicIds()) {
                    if (!topicRepository.existsById(topicId)) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseDTO<>(false, "Topic with id " + topicId + " does not exist", null));
                    }
                }
                question.get().setTopicIds(updateQuestionDTO.topicIds());
            }

            Question updatedQuestion = questionRepository.save(question.get());

            Map<UUID, String> topicMap = topicRepository.findAllById(updatedQuestion.getTopicIds()).stream()
                    .collect(Collectors.toMap(Topic::getId, Topic::getName));

            List<String> topicNames = updatedQuestion.getTopicIds().stream()
                    .map(topicMap::get)
                    .filter(Objects::nonNull)
                    .toList();

            QuestionResponseDTO questionResponseDTO = new QuestionResponseDTO(
                    updatedQuestion.getId(),
                    updatedQuestion.getLink(),
                    updatedQuestion.getTitle(),
                    updatedQuestion.getDifficulty(),
                    updatedQuestion.isSolved(),
                    updatedQuestion.isReviseLater(),
                    topicNames,
                    updatedQuestion.getNoteId(),
                    updatedQuestion.getCreatedAt(),
                    updatedQuestion.getSolveHistory()
            );
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(true, null, questionResponseDTO));
        } catch (Exception e) {
            log.error("An exception has occurred {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponseDTO<>(false, "An error occurred in the server", null));
        }
    }

    public ResponseEntity<ApiResponseDTO<Boolean>> deleteQuestion(String questionId, UUID createdBy) {
        try {
            UUID id = UUID.fromString(questionId);
            if (questionRepository.existsByIdAndCreatedBy(id, createdBy)) {
                questionRepository.deleteById(id);
                return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(true, null, true));
            } else {
                return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(false, "Question of id:" + questionId + " does not exist", false));
            }
        } catch (Exception e) {
            log.error("An exception has occurred {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponseDTO<>(false, "An error occurred in the server", false));
        }
    }

    public ResponseEntity<ApiResponseDTO<List<QuestionResponseDTO>>> getRandomQuestions(
            List<String> topicNames,
            String difficulty,
            int count,
            UUID createdBy
    ) {
        try {
            List<Criteria> filters = new ArrayList<>();
            filters.add(Criteria.where("createdBy").is(createdBy));
            filters.add(Criteria.where("solved").is(false));

            // Difficulty filter
            if (difficulty != null && !difficulty.isBlank()) {
                filters.add(Criteria.where("difficulty").is(Difficulty.valueOf(difficulty.toUpperCase())));
            }

            if (topicNames != null && !topicNames.isEmpty()) {
                List<UUID> topicIds = topicNames.stream()
                        .filter(topic -> topicRepository.existsByNameAndCreatedBy(topic, createdBy))
                        .map(topic -> topicRepository.findByNameAndCreatedBy(topic, createdBy))
                        .filter(Optional::isPresent)
                        .map(opt -> opt.get().getId())
                        .toList();

                if (!topicIds.isEmpty()) {
                    filters.add(Criteria.where("topicIds").in(topicIds));
                }
            }

            Criteria criteria = new Criteria();
            criteria.andOperator(filters.toArray(new Criteria[0]));

            Aggregation agg = Aggregation.newAggregation(
                    Aggregation.match(criteria),
                    Aggregation.sample(count)
            );

            AggregationResults<Question> results =
                    mongoTemplate.aggregate(agg, "questions", Question.class);

            List<Question> questions = results.getMappedResults();

            Set<UUID> allTopicIds = questions.stream()
                    .flatMap(q -> q.getTopicIds().stream())
                    .collect(Collectors.toSet());

            Map<UUID, String> topicMap = topicRepository.findAllById(allTopicIds).stream()
                    .collect(Collectors.toMap(Topic::getId, Topic::getName));

            List<QuestionResponseDTO> questionResponseDTOS = questions.stream()
                    .map(question -> {
                        List<String> topics = question.getTopicIds().stream()
                                .map(topicMap::get) // lookup from pre-fetched map
                                .filter(Objects::nonNull)
                                .toList();

                        return new QuestionResponseDTO(
                                question.getId(),
                                question.getLink(),
                                question.getTitle(),
                                question.getDifficulty(),
                                question.isSolved(),
                                question.isReviseLater(),
                                topics,
                                question.getNoteId(),
                                question.getCreatedAt(),
                                question.getSolveHistory()
                        );
                    })
                    .toList();

            return ResponseEntity.ok(new ApiResponseDTO<>(true, null, questionResponseDTOS));
        } catch (Exception e) {
            log.error("An exception has occurred {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO<>(false, "An error occurred in the server", null));
        }
    }

    public ResponseEntity<ApiResponseDTO<QuestionTimelineDTO>> getQuestionTimeline(String questionId, UUID createdBy) {
        try {
            Question question = questionRepository.findByIdAndCreatedBy(UUID.fromString(questionId), createdBy).orElse(null);
            if (question == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponseDTO<>(false, "Question with id:" + questionId + " does not exist", null));
            }

            QuestionTimelineDTO questionTimelineDTO = objectMapper.convertValue(question, QuestionTimelineDTO.class);
            return ResponseEntity.ok(new ApiResponseDTO<>(true, null, questionTimelineDTO));
        } catch (Exception e) {
            log.error("An exception has occurred {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO<>(false, "An error occurred in the server", null));
        }
    }

    public ResponseEntity<ApiResponseDTO<Boolean>> checkIfQuestionExists(@Valid CheckQuestionExistsDTO checkQuestionExistsDTO, UUID createdBy) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(true, null, questionRepository.existsByTitleAndCreatedBy(checkQuestionExistsDTO.title(), createdBy)));
        } catch (Exception e) {
            log.error("An exception has occurred {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO<>(false, "An error occurred in the server", null));
        }
    }

    public ResponseEntity<ApiResponseDTO<List<QuestionResponseDTO>>> getQuestionsBasedOnIds(@Valid QuestionBasedOnIdsDTO questionBasedOnIdsDTO) {
        try {
            List<Question> questions = questionRepository.findAllById(questionBasedOnIdsDTO.questionsIds());
            Set<UUID> allTopicIds = questions.stream()
                    .flatMap(q -> q.getTopicIds().stream())
                    .collect(Collectors.toSet());

            Map<UUID, String> topicMap = topicRepository.findAllById(allTopicIds).stream()
                    .collect(Collectors.toMap(Topic::getId, Topic::getName));

            List<QuestionResponseDTO> questionResponseDTOS = questions.stream()
                    .map(question -> {
                        List<String> topicNames = question.getTopicIds().stream()
                                .map(topicMap::get) // lookup from pre-fetched map
                                .filter(Objects::nonNull)
                                .toList();

                        return new QuestionResponseDTO(
                                question.getId(),
                                question.getLink(),
                                question.getTitle(),
                                question.getDifficulty(),
                                question.isSolved(),
                                question.isReviseLater(),
                                topicNames,
                                question.getNoteId(),
                                question.getCreatedAt(),
                                question.getSolveHistory()
                        );
                    })
                    .toList();
            return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDTO<>(true, null, questionResponseDTOS));
        } catch (Exception e) {
            log.error("An exception has occurred {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO<>(false, "An error occurred in the server", null));
        }
    }

    @RateLimiter(name = "autofillRateLimiter", fallbackMethod = "rateLimitedFallback")
    @Bulkhead(name = "autofillBulkhead", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "bulkHeadFallback")
    public ResponseEntity<ApiResponseDTO<QuestionAutofillDTO>> autofillQuestion(@Valid AutofillRequestDTO autofillRequestDTO) {
        return autofillClient.autofillQuestion(autofillRequestDTO);
    }

    public ResponseEntity<ApiResponseDTO<QuestionAutofillDTO>> rateLimitedFallback(
            AutofillRequestDTO autofillRequestDTO,
            Throwable t) {

        ApiResponseDTO<QuestionAutofillDTO> response =
                new ApiResponseDTO<>(
                        false,
                        "Rate limit exceeded. Please try again after some time.",
                        null
                );

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS) // 429
                .body(response);
    }

    public ResponseEntity<ApiResponseDTO<QuestionAutofillDTO>> bulkHeadFallback(
            AutofillRequestDTO autofillRequestDTO,
            Throwable t) {

        ApiResponseDTO<QuestionAutofillDTO> response =
                new ApiResponseDTO<>(
                        false,
                        "Service is busy. Please try again shortly.",
                        null
                );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE) // 503
                .body(response);
    }

}
