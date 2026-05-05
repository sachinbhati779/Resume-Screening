package com.resumescreening.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.resumescreening.dto.AnswerEvaluationResultDTO;
import com.resumescreening.model.InterviewQuestion;
import java.util.List;
import org.junit.jupiter.api.Test;

class InterviewFlowServiceTest {

    @Test
    void evaluatesAnswerWithKeywordAndCompletenessSignals() {
        AnswerEvaluatorService evaluator = new AnswerEvaluatorService(false, "");
        InterviewQuestion question = new InterviewQuestion();
        question.setMarks(25);
        question.setExpectedKeywords(List.of("rest", "http", "json", "status"));

        AnswerEvaluationResultDTO result = evaluator.evaluate(
                question,
                "I would expose REST endpoints over HTTP, use JSON payloads, and return clear status codes.");

        assertThat(result.score()).isGreaterThan(15);
        assertThat(result.feedback()).isNotBlank();
    }
}
