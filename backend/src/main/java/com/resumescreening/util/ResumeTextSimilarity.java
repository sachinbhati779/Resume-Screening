package com.resumescreening.util;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ResumeTextSimilarity {

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "and", "are", "as", "at", "be", "by", "for", "from",
            "has", "have", "in", "is", "it", "of", "on", "or", "that", "the",
            "to", "with", "will", "you", "your"
    );

    private ResumeTextSimilarity() {
    }

    public static double cosineSimilarity(String left, String right) {
        Map<String, Long> leftVector = tokenFrequency(left);
        Map<String, Long> rightVector = tokenFrequency(right);
        if (leftVector.isEmpty() || rightVector.isEmpty()) {
            return 0;
        }

        double dotProduct = 0;
        for (Map.Entry<String, Long> entry : leftVector.entrySet()) {
            dotProduct += entry.getValue() * rightVector.getOrDefault(entry.getKey(), 0L);
        }
        if (dotProduct == 0) {
            return 0;
        }

        double leftMagnitude = magnitude(leftVector);
        double rightMagnitude = magnitude(rightVector);
        if (leftMagnitude == 0 || rightMagnitude == 0) {
            return 0;
        }
        return dotProduct / (leftMagnitude * rightMagnitude);
    }

    public static List<String> tokens(String value) {
        String normalized = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9+#.\\s-]", " ")
                .replace('-', ' ');
        return Arrays.stream(normalized.split("\\s+"))
                .map(String::trim)
                .filter(token -> token.length() > 1)
                .filter(token -> !STOP_WORDS.contains(token))
                .toList();
    }

    private static Map<String, Long> tokenFrequency(String value) {
        return tokens(value).stream()
                .collect(Collectors.groupingBy(Function.identity(), HashMap::new, Collectors.counting()));
    }

    private static double magnitude(Map<String, Long> vector) {
        return Math.sqrt(vector.values().stream()
                .mapToDouble(value -> value * value)
                .sum());
    }
}
