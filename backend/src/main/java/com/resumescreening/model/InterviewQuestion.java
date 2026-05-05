package com.resumescreening.model;

import java.util.ArrayList;
import java.util.List;

public class InterviewQuestion {

    private Long id;
    private Long sessionId;
    private String questionText;
    private List<String> expectedKeywords = new ArrayList<>();
    private int marks;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public List<String> getExpectedKeywords() {
        return expectedKeywords;
    }

    public void setExpectedKeywords(List<String> expectedKeywords) {
        this.expectedKeywords = expectedKeywords == null ? new ArrayList<>() : new ArrayList<>(expectedKeywords);
    }

    public int getMarks() {
        return marks;
    }

    public void setMarks(int marks) {
        this.marks = marks;
    }
}
