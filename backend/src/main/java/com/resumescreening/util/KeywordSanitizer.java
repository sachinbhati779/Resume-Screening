package com.resumescreening.util;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class KeywordSanitizer {

    private static final Set<String> GENERIC_RECRUITING_WORDS = Set.of(
            "ability", "able", "about", "applicant", "candidate", "candidates",
            "company", "excellent", "good", "great", "hiring", "ideal", "join",
            "looking", "must", "need", "needs", "passionate", "preferred",
            "required", "requires", "responsibilities", "role", "should",
            "skilled", "skills", "strong", "team", "work", "working"
    );

    private KeywordSanitizer() {
    }

    public static List<String> sanitize(Collection<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return List.of();
        }

        Set<String> sanitized = new LinkedHashSet<>();
        for (String keyword : keywords) {
            String normalized = normalize(keyword);
            if (isUsefulKeyword(normalized)) {
                sanitized.add(normalized);
            }
        }
        return List.copyOf(sanitized);
    }

    public static boolean isUsefulKeyword(String keyword) {
        String normalized = normalize(keyword);
        return normalized.length() >= 2
                && !GENERIC_RECRUITING_WORDS.contains(normalized)
                && normalized.matches(".*[a-z0-9+#.].*");
    }

    private static String normalize(String keyword) {
        return keyword == null
                ? ""
                : keyword.trim()
                        .toLowerCase(Locale.ROOT)
                        .replaceAll("[^a-z0-9+#.\\s-]", "")
                        .replaceAll("\\s+", " ");
    }
}
