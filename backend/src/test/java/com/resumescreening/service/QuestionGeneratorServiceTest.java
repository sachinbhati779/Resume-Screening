package com.resumescreening.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.resumescreening.model.InterviewQuestion;
import com.resumescreening.model.JobRole;
import com.resumescreening.model.Resume;
import java.util.List;
import org.junit.jupiter.api.Test;

class QuestionGeneratorServiceTest {

    @Test
    void generatesRoleAndSkillBasedQuestions() {
        Resume resume = new Resume();
        resume.setSkills(List.of("Java", "SQL", "React"));
        JobRole role = new JobRole();
        role.setRoleName("AI Interview Engineer");

        List<InterviewQuestion> questions = new QuestionGeneratorService().generateQuestions(10L, resume, role);

        assertThat(questions).hasSize(4);
        assertThat(questions.get(0).getQuestionText()).contains("OOP");
        assertThat(questions).allMatch(question -> question.getMarks() == 25);
    }
}
