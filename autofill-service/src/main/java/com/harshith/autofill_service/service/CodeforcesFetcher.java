package com.harshith.autofill_service.service;

import com.harshith.autofill_service.dto.Difficulty;
import com.harshith.autofill_service.dto.QuestionAutofillDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class CodeforcesFetcher {

    private static final String CF_API = "https://codeforces.com/api/problemset.problems";
    private final RestClient restClient;

    public CodeforcesFetcher(RestClient restClient) {
        this.restClient = restClient;
    }

    public QuestionAutofillDTO fetch(String link) {
        try {
            // Example link: https://codeforces.com/problemset/problem/4/A
            String[] parts = link.split("/");
            String contestId = parts[parts.length - 2]; // "4"
            String index = parts[parts.length - 1];     // "A"

            // Fetch full problemset (not ideal, but Codeforces API doesn’t allow single-problem fetch)
            Map<String, Object> response = restClient.get()
                    .uri(CF_API)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            if (response == null || !"OK".equals(response.get("status"))) {
                return null;
            }

            Map<String, Object> result = (Map<String, Object>) response.get("result");
            List<Map<String, Object>> problems = (List<Map<String, Object>>) result.get("problems");

            // Find the specific problem
            for (Map<String, Object> problem : problems) {
                if (String.valueOf(problem.get("contestId")).equals(contestId)
                        && problem.get("index").equals(index)) {

                    String title = (String) problem.get("name");
                    String difficulty = "Unknown";
                    if (problem.containsKey("rating")) {
                        int rating = (Integer) problem.get("rating");
                        if (rating <= 1200) {
                            difficulty = Difficulty.EASY.name();
                        } else if (rating <= 2000) {
                            difficulty = Difficulty.MEDIUM.name();
                        } else {
                            difficulty = Difficulty.HARD.name();
                        }
                    }

                    List<String> topics = (List<String>) problem.get("tags");

                    return new QuestionAutofillDTO("CODEFORCES", title, topics, Difficulty.valueOf(difficulty));
                }
            }

            return null;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
