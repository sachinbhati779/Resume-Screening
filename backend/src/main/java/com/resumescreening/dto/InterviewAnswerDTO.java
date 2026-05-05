package com.resumescreening.dto;

import jakarta.validation.constraints.NotBlank;

public record InterviewAnswerDTO(
        Long questionId,
        @NotBlank String answerText,
        Double score,
        String feedback,
        Boolean completed,
        InterviewQuestionDTO nextQuestion,
        InterviewResultDTO result
) {
}
