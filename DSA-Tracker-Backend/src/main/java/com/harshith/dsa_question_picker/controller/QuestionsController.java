package com.harshith.dsa_question_picker.controller;

import com.harshith.dsa_question_picker.dto.ApiResponseDTO;
import com.harshith.dsa_question_picker.dto.question.*;
import com.harshith.dsa_question_picker.model.User;
import com.harshith.dsa_question_picker.service.QuestionsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/questions")
public class QuestionsController {
    private final QuestionsService questionsService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<AllQuestionsDTO>> getAllQuestions(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(name = "key", required = false) String key,
            @RequestParam(name = "topics", required = false) List<String> topics,
            @RequestParam(name = "difficulty", required = false) String difficulty,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "desc") String sortDir,
            @AuthenticationPrincipal User oAuth2User
    ) {
        UUID createdBy = oAuth2User.getId();
        return questionsService.getAllQuestion(
                page,
                pageSize,
                key,
                topics,
                difficulty,
                status,
                sortBy,
                sortDir,
                createdBy
        );
    }

    @PostMapping("/getQuestionsBasedOnIds")
    public ResponseEntity<ApiResponseDTO<List<QuestionResponseDTO>>> getQuestionsBasedOnIds(@Valid @RequestBody QuestionBasedOnIdsDTO questionBasedOnIdsDTO) {
        return questionsService.getQuestionsBasedOnIds(questionBasedOnIdsDTO);
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<QuestionResponseDTO>> addQuestion(@Valid @RequestBody PostQuestionDTO postQuestionDTO, @AuthenticationPrincipal User oAuth2User) {
        UUID createdBy = oAuth2User.getId();
        return questionsService.addQuestion(postQuestionDTO, createdBy);
    }

    @PutMapping("/{questionId}")
    public ResponseEntity<ApiResponseDTO<QuestionResponseDTO>> updateQuestion(@Valid @RequestBody UpdateQuestionDTO updateQuestionDTO, @PathVariable("questionId") String questionId, @AuthenticationPrincipal User oAuth2User) {
        UUID createdBy = oAuth2User.getId();
        return questionsService.updateQuestion(updateQuestionDTO, questionId, createdBy);
    }

    @DeleteMapping("/{questionId}")
    public ResponseEntity<ApiResponseDTO<Boolean>> deleteQuestion(@PathVariable("questionId") String questionId, @AuthenticationPrincipal User oAuth2User) {
        UUID createdBy = oAuth2User.getId();
        return questionsService.deleteQuestion(questionId, createdBy);
    }

    @PostMapping("/checkIfExists")
    public ResponseEntity<ApiResponseDTO<Boolean>> checkQuestionExists(@Valid @RequestBody CheckQuestionExistsDTO checkQuestionExistsDTO, @AuthenticationPrincipal User oAuth2User) {
        UUID createdBy = oAuth2User.getId();
        return questionsService.checkIfQuestionExists(checkQuestionExistsDTO, createdBy);
    }

    @GetMapping("/random")
    public ResponseEntity<ApiResponseDTO<List<QuestionResponseDTO>>> getRandomQuestions(
            @RequestParam(name = "topics", required = false) List<String> topics,
            @RequestParam(name = "difficulty", required = false) String difficulty,
            @RequestParam(name = "count", defaultValue = "1") int count,
            @AuthenticationPrincipal User oAuth2User
    ) {
        UUID createdBy = oAuth2User.getId();
        return questionsService.getRandomQuestions(topics, difficulty, count, createdBy);
    }

    @GetMapping("/timeline/{questionId}")
    public ResponseEntity<ApiResponseDTO<QuestionTimelineDTO>> getQuestionTimeline(@PathVariable("questionId") String questionId, @AuthenticationPrincipal User oAuth2User) {
        UUID createdBy = oAuth2User.getId();
        return questionsService.getQuestionTimeline(questionId, createdBy);
    }

    @PostMapping("/autofill")
    public ResponseEntity<ApiResponseDTO<QuestionAutofillDTO>> autofillQuestion(@Valid @RequestBody AutofillRequestDTO autofillRequestDTO) {
        return questionsService.autofillQuestion(autofillRequestDTO);
    }

}
