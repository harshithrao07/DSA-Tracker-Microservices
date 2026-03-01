package com.harshith.dsa_question_picker.controller;

import com.harshith.dsa_question_picker.dto.ApiResponseDTO;
import com.harshith.dsa_question_picker.dto.topic.PostTopicDTO;
import com.harshith.dsa_question_picker.dto.topic.TopicResponseDTO;
import com.harshith.dsa_question_picker.model.User;
import com.harshith.dsa_question_picker.service.TopicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/topics")
public class TopicController {
    private final TopicService topicService;

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<TopicResponseDTO>>> getAllTopics(@AuthenticationPrincipal User oAuth2User) {
        UUID createdBy = oAuth2User.getId();
        return topicService.getAllTopics(createdBy);
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<TopicResponseDTO>> postTopic(@Valid @RequestBody PostTopicDTO postTopicDTO, @AuthenticationPrincipal User oAuth2User) {
        UUID createdBy = oAuth2User.getId();
        return topicService.postTopic(postTopicDTO, createdBy);
    }

}
