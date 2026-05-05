package com.resumescreening.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class StringListConverter {

    private StringListConverter() {
    }

    public static String toCsv(Collection<String> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        return values.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .reduce((left, right) -> left + "," + right)
                .orElse("");
    }

    public static List<String> fromCsv(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

    public static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public static boolean containsNormalized(String text, String keyword) {
        String normalizedText = normalize(text);
        String normalizedKeyword = normalize(keyword);
        return !normalizedText.isBlank()
                && !normalizedKeyword.isBlank()
                && normalizedText.contains(normalizedKeyword);
    }

    public static long countMatches(Collection<String> expectedValues, Collection<String> actualValues) {
        if (expectedValues == null || expectedValues.isEmpty() || actualValues == null || actualValues.isEmpty()) {
            return 0;
        }
        return expectedValues.stream()
                .filter(expected -> actualValues.stream()
                        .anyMatch(actual -> isPartialMatch(actual, expected)))
                .count();
    }

    public static boolean isPartialMatch(String actual, String expected) {
        String normalizedActual = normalize(actual);
        String normalizedExpected = normalize(expected);
        if (normalizedActual.isBlank() || normalizedExpected.isBlank()) {
            return false;
        }
        return normalizedActual.equalsIgnoreCase(normalizedExpected)
                || normalizedActual.contains(normalizedExpected)
                || normalizedExpected.contains(normalizedActual);
    }
}
