package com.resumescreening.service;

import com.resumescreening.model.InterviewQuestion;
import com.resumescreening.model.JobRole;
import com.resumescreening.model.Resume;
import com.resumescreening.util.StringListConverter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class QuestionGeneratorService {

    private static final int QUESTION_MARKS = 25;

    public List<InterviewQuestion> generateQuestions(Long sessionId, Resume resume, JobRole role) {
        List<InterviewQuestion> questions = new ArrayList<>();
        for (String skill : resume.getSkills()) {
            questions.add(questionForSkill(sessionId, skill));
            if (questions.size() == 3) {
                break;
            }
        }
        questions.add(generalProjectQuestion(sessionId, role));
        while (questions.size() < 4) {
            questions.add(build(sessionId,
                    "Describe how you approach problem solving, tradeoffs, and communication in a delivery team.",
                    List.of("problem", "tradeoff", "communication", "team", "delivery")));
        }
        return questions.stream().limit(4).toList();
    }

    private InterviewQuestion questionForSkill(Long sessionId, String skill) {
        String normalized = StringListConverter.normalize(skill);
        if (normalized.contains("java")) {
            return build(sessionId,
                    "Explain how you would use OOP, Collections, JVM behavior, and exception handling in a production service.",
                    List.of("oop", "collections", "jvm", "exception", "service"));
        }
        if (normalized.contains("sql") || normalized.contains("mysql")) {
            return build(sessionId,
                    "How would you optimize a SQL query that joins candidate, resume, and interview tables?",
                    List.of("join", "index", "normalization", "query", "explain"));
        }
        if (normalized.contains("react") || normalized.contains("frontend") || normalized.contains("typescript")) {
            return build(sessionId,
                    "Describe how React components, state, props, and hooks should communicate with a REST backend.",
                    List.of("component", "state", "props", "hooks", "rest"));
        }
        if (normalized.contains("web") || normalized.contains("rest") || normalized.contains("api")) {
            return build(sessionId,
                    "Walk through HTTP and REST design choices for a resume screening workflow.",
                    List.of("http", "rest", "endpoint", "status", "json"));
        }
        return build(sessionId,
                "Describe a project where you used " + skill + " to solve a practical business problem.",
                List.of(skill, "problem", "tradeoff", "result"));
    }

    private InterviewQuestion generalProjectQuestion(Long sessionId, JobRole role) {
        return build(sessionId,
                "Which project best proves your fit for " + role.getRoleName() + ", and what would you improve now?",
                List.of("project", "role", "improve", "impact", "communication"));
    }

    private InterviewQuestion build(Long sessionId, String text, List<String> expectedKeywords) {
        InterviewQuestion question = new InterviewQuestion();
        question.setSessionId(sessionId);
        question.setQuestionText(text);
        question.setExpectedKeywords(expectedKeywords);
        question.setMarks(QUESTION_MARKS);
        return question;
    }
}
