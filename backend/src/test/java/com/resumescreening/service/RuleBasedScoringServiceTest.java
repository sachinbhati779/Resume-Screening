package com.resumescreening.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.resumescreening.exception.IncompleteResumeException;
import com.resumescreening.model.JobRole;
import com.resumescreening.model.Resume;
import com.resumescreening.util.ScreeningStatusResolver;
import java.util.List;
import org.junit.jupiter.api.Test;

class RuleBasedScoringServiceTest {

    private final RuleBasedScoringService scoringService = new RuleBasedScoringService();

    @Test
    void scoresValidResumeAndShortlistsStrongCandidate() {
        Resume resume = resume();
        JobRole role = role();

        double score = scoringService.calculateScore(resume, role);

        assertThat(score).isGreaterThanOrEqualTo(80);
        assertThat(ScreeningStatusResolver.resolve(score)).isEqualTo("SHORTLISTED");
    }

    @Test
    void rejectsIncompleteResumeBeforeScoring() {
        Resume resume = resume();
        resume.setSummary("");

        assertThatThrownBy(() -> scoringService.calculateScore(resume, role()))
                .isInstanceOf(IncompleteResumeException.class)
                .hasMessageContaining("Summary");
    }

    private Resume resume() {
        Resume resume = new Resume();
        resume.setCandidateName("Aarav Menon");
        resume.setEmail("aarav@example.com");
        resume.setSkills(List.of("Java", "SQL", "React", "REST"));
        resume.setExperienceYears(5);
        resume.setEducation("B.Tech Computer Science");
        resume.setProjects(List.of("REST hiring dashboard", "SQL ranking pipeline"));
        resume.setSummary("Built Java REST services with SQL indexes and React frontend integration.");
        resume.setAppliedRole("Frontend AI Engineer");
        return resume;
    }

    private JobRole role() {
        JobRole role = new JobRole();
        role.setId(1L);
        role.setRoleName("Frontend AI Engineer");
        role.setRequiredSkills(List.of("Java", "SQL", "React", "REST"));
        role.setMinExperience(4);
        role.setRequiredEducation("Computer Science");
        role.setKeywords(List.of("rest", "sql", "react", "java"));
        return role;
    }
}
