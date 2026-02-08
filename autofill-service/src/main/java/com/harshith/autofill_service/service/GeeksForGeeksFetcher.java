package com.harshith.autofill_service.service;

import com.harshith.autofill_service.dto.Difficulty;
import com.harshith.autofill_service.dto.QuestionAutofillDTO;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeeksForGeeksFetcher {

    // Mock problems for GFG
    private static final Map<String, QuestionAutofillDTO> MOCK_PROBLEMS = new HashMap<>();
    static {
        MOCK_PROBLEMS.put("check-if-array-is-sorted-and-rotated",
                new QuestionAutofillDTO("GeeksforGeeks", "Check if Array is Sorted and Rotated", List.of("Arrays"), Difficulty.EASY));
        MOCK_PROBLEMS.put("reverse-linked-list",
                new QuestionAutofillDTO("GeeksforGeeks", "Reverse Linked List", List.of("Linked List"), Difficulty.EASY));
        MOCK_PROBLEMS.put("balanced-binary-tree",
                new QuestionAutofillDTO("GeeksforGeeks", "Balanced Binary Tree", List.of("Binary Tree"), Difficulty.EASY));
        MOCK_PROBLEMS.put("longest-substring-without-repeating-characters",
                new QuestionAutofillDTO("GeeksforGeeks", "Longest Substring Without Repeating Characters", List.of("Strings", "Sliding Window"), Difficulty.MEDIUM));
        MOCK_PROBLEMS.put("two-sum",
                new QuestionAutofillDTO("GeeksforGeeks", "Two Sum", List.of("Arrays"), Difficulty.EASY));
        MOCK_PROBLEMS.put("median-of-two-sorted-arrays",
                new QuestionAutofillDTO("GeeksforGeeks", "Median of Two Sorted Arrays", List.of("Binary Search", "Arrays"), Difficulty.HARD));
        MOCK_PROBLEMS.put("valid-parentheses",
                new QuestionAutofillDTO("GeeksforGeeks", "Valid Parentheses", List.of("Stack"), Difficulty.EASY));
        MOCK_PROBLEMS.put("climbing-stairs",
                new QuestionAutofillDTO("GeeksforGeeks", "Climbing Stairs", List.of("Dynamic Programming"), Difficulty.EASY));
    }

    private static final Map<String, String> KEYWORD_TOPIC_MAP = Map.ofEntries(
            Map.entry("array", "Arrays"),
            Map.entry("matrix", "Arrays"),
            Map.entry("string", "Strings"),
            Map.entry("substring", "Strings"),
            Map.entry("palindrome", "Strings"),
            Map.entry("linked", "Linked List"),
            Map.entry("list", "Linked List"),
            Map.entry("tree", "Binary Tree"),
            Map.entry("binary-tree", "Binary Tree"),
            Map.entry("bst", "BST"),
            Map.entry("stack", "Stack"),
            Map.entry("queue", "Queue"),
            Map.entry("graph", "Graphs"),
            Map.entry("dfs", "DFS"),
            Map.entry("bfs", "BFS"),
            Map.entry("dp", "Dynamic Programming"),
            Map.entry("dynamic", "Dynamic Programming"),
            Map.entry("hash", "Hash Table"),
            Map.entry("map", "Hash Table"),
            Map.entry("heap", "Heap"),
            Map.entry("priority", "Heap"),
            Map.entry("trie", "Trie"),
            Map.entry("bit", "Bit Manipulation"),
            Map.entry("greedy", "Greedy"),
            Map.entry("two-pointer", "Two Pointers"),
            Map.entry("sliding", "Sliding Window"),
            Map.entry("window", "Sliding Window"),
            Map.entry("backtrack", "Backtracking"),
            Map.entry("search", "Binary Search"),
            Map.entry("sort", "Sorting"),
            Map.entry("divide", "Divide and Conquer"),
            Map.entry("union", "Union Find")
    );

    public static QuestionAutofillDTO fetch(String url) {
        String problemId = extractProblemId(url);
        if (problemId == null) return null;

        // Return mock if available
        if (MOCK_PROBLEMS.containsKey(problemId)) {
            return MOCK_PROBLEMS.get(problemId);
        }

        // Fallback: generate title & topic
        String title = generateTitleFromId(problemId);
        List<String> topics = detectTopicsFromId(problemId);

        return new QuestionAutofillDTO("GeeksforGeeks", title, topics, Difficulty.MEDIUM);
    }

    private static String extractProblemId(String url) {
        try {
            URI uri = new URI(url);
            String path = uri.getPath();
            String[] segments = path.split("/");

            for (int i = 0; i < segments.length; i++) {
                if (segments[i].equalsIgnoreCase("problems") || segments[i].equalsIgnoreCase("practice-problems")) {
                    if (i + 1 < segments.length) {
                        return segments[i + 1];
                    }
                }
            }

            // fallback: last segment
            return segments.length > 0 ? segments[segments.length - 1] : null;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private static String generateTitleFromId(String id) {
        String[] words = id.split("-");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            sb.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

    private static List<String> detectTopicsFromId(String id) {
        List<String> topics = new ArrayList<>();
        String lowerId = id.toLowerCase();

        for (Map.Entry<String, String> entry : KEYWORD_TOPIC_MAP.entrySet()) {
            if (lowerId.contains(entry.getKey())) {
                topics.add(entry.getValue());
            }
        }

        if (topics.isEmpty()) {
            topics.add("Data Structures"); // default
        }

        return topics;
    }
}
