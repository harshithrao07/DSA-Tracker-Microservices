package com.harshith.autofill_service.service;

import com.harshith.autofill_service.dto.ApiResponseDTO;
import com.harshith.autofill_service.dto.AutofillRequestDTO;
import com.harshith.autofill_service.dto.QuestionAutofillDTO;
import com.harshith.autofill_service.utils.Utility;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URL;

@Service
public class QuestionAutofillService {
    private final LeetCodeFetcher leetCodeFetcher;
    private final CodeforcesFetcher codeforcesFetcher;
    private final GeeksForGeeksFetcher geeksForGeeksFetcher;

    public QuestionAutofillService(LeetCodeFetcher leetCodeFetcher, CodeforcesFetcher codeforcesFetcher, GeeksForGeeksFetcher geeksForGeeksFetcher) {
        this.leetCodeFetcher = leetCodeFetcher;
        this.codeforcesFetcher = codeforcesFetcher;
        this.geeksForGeeksFetcher = geeksForGeeksFetcher;
    }

    public ResponseEntity<ApiResponseDTO<QuestionAutofillDTO>> autofillQuestion(AutofillRequestDTO autofillRequestDTO) {
        try {
            String platform = detectPlatform(autofillRequestDTO.link());

            if (platform == null || platform.equals("UNKNOWN")) {
                return ResponseEntity.badRequest().body(
                        new ApiResponseDTO<>(false, "Unsupported or invalid platform", null)
                );
            }

            QuestionAutofillDTO questionData = switch (platform) {
                case "LEETCODE" -> leetCodeFetcher.fetch(autofillRequestDTO.link());
                case "CODEFORCES" -> codeforcesFetcher.fetch(autofillRequestDTO.link());
                case "GEEKSFORGEEKS" -> geeksForGeeksFetcher.fetch(autofillRequestDTO.link());
                default -> null;
            };


            if (questionData == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new ApiResponseDTO<>(false, "Could not fetch problem details", null)
                );
            }

            return ResponseEntity.ok(
                    new ApiResponseDTO<>(true, null, questionData)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO<>(false, "An error occurred in the server", null));
        }
    }

    private String detectPlatform(String link) {
        try {
            if (!Utility.isValidUrl(link)) {
                return null;
            }

            URL url = new URL(link);
            String host = url.getHost().toLowerCase();

            if (host.contains("leetcode.com")) {
                return "LEETCODE";
            } else if (host.contains("codeforces.com")) {
                return "CODEFORCES";
            } else if (host.contains("geeksforgeeks.org")) {
                return "GEEKSFORGEEKS";
            } else {
                return "UNKNOWN";
            }
        } catch (Exception e) {
            return null;
        }
    }

}
