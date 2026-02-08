package com.harshith.autofill_service.service;

import com.harshith.autofill_service.dto.Difficulty;
import com.harshith.autofill_service.dto.QuestionAutofillDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class LeetCodeFetcher {

    private static final String LEETCODE_GRAPHQL = "https://leetcode.com/graphql";
    private final RestClient restClient;

    public LeetCodeFetcher(RestClient restClient) {
        this.restClient = restClient;
    }

    public QuestionAutofillDTO fetch(String link) {
        try {
            // Extract slug from URL: https://leetcode.com/problems/two-sum/ -> "two-sum"
            String[] parts = link.split("/");
            String slug = null;
            for (int i = 0; i < parts.length; i++) {
                if ("problems".equals(parts[i]) && i + 1 < parts.length) {
                    slug = parts[i + 1]; // the real slug
                    break;
                }
            }

            if (slug == null || slug.isBlank()) {
                throw new IllegalArgumentException("Invalid LeetCode problem URL: " + link);
            }


            String query = """
                        query getQuestionDetail($titleSlug: String!) {
                          question(titleSlug: $titleSlug) {
                            title
                            difficulty
                            topicTags { name }
                          }
                        }
                    """;

            Map<String, Object> response = restClient.post()
                    .uri(LEETCODE_GRAPHQL)
                    .body(Map.of(
                            "query", query,
                            "variables", Map.of("titleSlug", slug)
                    ))
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            if (response == null || response.get("data") == null) {
                return null;
            }

            Map<String, Object> data = (Map<String, Object>) ((Map<String, Object>) response.get("data")).get("question");

            String title = (String) data.get("title");
            String rawDifficulty = (String) data.get("difficulty");
            Difficulty difficulty = switch (rawDifficulty.toUpperCase()) {
                case "EASY" -> Difficulty.EASY;
                case "MEDIUM" -> Difficulty.MEDIUM;
                case "HARD" -> Difficulty.HARD;
                default -> Difficulty.MEDIUM; // fallback
            };

            List<Map<String, String>> tags = (List<Map<String, String>>) data.get("topicTags");
            List<String> topics = tags.stream().map(tag -> tag.get("name")).toList();

            return new QuestionAutofillDTO("LEETCODE", title, topics, difficulty);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
