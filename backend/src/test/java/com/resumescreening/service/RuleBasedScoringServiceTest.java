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
    void searchesExtractedResumeTextForRequiredSkillsWhenSkillSectionIsMissing() {
        Resume resume = resume();
        resume.setSkills(List.of());
        resume.setEducation("");
        resume.setProjects(List.of());
        resume.setExtractedText("""
                Aarav Menon
                aarav@example.com
                Phone: +91 98765 43210
                Summary: Frontend AI engineer with strong experience building hiring workflows, recruiter dashboards, and structured interview tools.
                Skills
                Java, SQL, React, REST
                Education
                B.Tech Computer Science
                Experience
                5 years experience building Java REST services, SQL indexes, and React dashboards for production hiring systems.
                Projects
                REST hiring dashboard, SQL ranking pipeline, candidate screening API, and interview workflow automation.
                """);

        double score = scoringService.calculateScore(resume, role());

        assertThat(score).isGreaterThanOrEqualTo(80);
    }

    @Test
    void rejectsResumeWithNoSearchableContentBeforeScoring() {
        Resume resume = resume();
        resume.setSkills(List.of());
        resume.setProjects(List.of());
        resume.setSummary("");
        resume.setEducation("");
        resume.setExtractedText("");

        assertThatThrownBy(() -> scoringService.calculateScore(resume, role()))
                .isInstanceOf(IncompleteResumeException.class)
                .hasMessageContaining("valid resume");
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
        resume.setExtractedText("""
                Aarav Menon
                aarav@example.com
                Phone: +91 98765 43210

                Summary
                Built Java REST services with SQL indexes and React frontend integration for recruiter workflows, candidate ranking, and interview automation.

                Skills
                Java, SQL, React, REST

                Education
                B.Tech Computer Science

                Experience
                5 years building production APIs, dashboard workflows, SQL-backed ranking systems, and structured hiring tools.

                Projects
                REST hiring dashboard, SQL ranking pipeline, resume screening workflow, and AI interview console.
                """);
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
