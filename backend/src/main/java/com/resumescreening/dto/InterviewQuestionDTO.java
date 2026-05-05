package com.resumescreening.dto;

public record InterviewQuestionDTO(
        Long questionId,
        Long sessionId,
        String questionText,
        int questionNumber,
        int totalQuestions,
        int marks
) {
}
