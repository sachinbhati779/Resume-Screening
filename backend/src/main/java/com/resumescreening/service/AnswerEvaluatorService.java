package com.resumescreening.service;

import com.resumescreening.dto.AnswerEvaluationResultDTO;
import com.resumescreening.model.InterviewQuestion;
import com.resumescreening.util.StringListConverter;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AnswerEvaluatorService {

    private final boolean openAiEnabled;
    private final String openAiApiKey;

    public AnswerEvaluatorService(
            @Value("${app.openai.enabled:false}") boolean openAiEnabled,
            @Value("${app.openai.api-key:}") String openAiApiKey) {
        this.openAiEnabled = openAiEnabled;
        this.openAiApiKey = openAiApiKey;
    }

    public AnswerEvaluationResultDTO evaluate(InterviewQuestion question, String answerText) {
        AnswerEvaluationResultDTO ruleBasedResult = evaluateWithRules(question, answerText);
        if (openAiEnabled && openAiApiKey != null && !openAiApiKey.isBlank()) {
            return enrichWithOptionalOpenAi(ruleBasedResult);
        }
        return ruleBasedResult;
    }

    private AnswerEvaluationResultDTO evaluateWithRules(InterviewQuestion question, String answerText) {
        List<String> words = Arrays.stream(StringListConverter.normalize(answerText).split("\\s+"))
                .filter(word -> !word.isBlank())
                .toList();
        long keywordMatches = question.getExpectedKeywords().stream()
                .filter(keyword -> StringListConverter.containsNormalized(answerText, keyword))
                .count();
        double keywordRatio = question.getExpectedKeywords().isEmpty()
                ? 0
                : keywordMatches / (double) question.getExpectedKeywords().size();

        double technicalCorrectness = keywordRatio * 50;
        double conceptClarity = Math.min(20, words.size() >= 40 ? 20 : words.size() / 2.0);
        double keywordScore = keywordRatio * 20;
        double communication = scoreCommunication(answerText, words.size());
        double rawScore = Math.min(100, technicalCorrectness + conceptClarity + keywordScore + communication);
        double scaledScore = round(question.getMarks() * (rawScore / 100));

        String feedback = keywordRatio >= 0.7
                ? "Strong answer with relevant technical keywords and clear practical framing."
                : "Answer is partially relevant; add more concrete implementation details and expected concepts.";

        return new AnswerEvaluationResultDTO(scaledScore, feedback);
    }

    private AnswerEvaluationResultDTO enrichWithOptionalOpenAi(AnswerEvaluationResultDTO result) {
        return new AnswerEvaluationResultDTO(
                result.score(),
                result.feedback() + " Optional semantic AI review can be plugged in here."
        );
    }

    private double scoreCommunication(String answerText, int wordCount) {
        if (answerText == null || answerText.isBlank()) {
            return 0;
        }
        double score = Math.min(7, wordCount / 8.0);
        if (answerText.contains(".") || answerText.contains(",")) {
            score += 3;
        }
        return Math.min(10, score);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
