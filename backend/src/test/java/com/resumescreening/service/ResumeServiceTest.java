package com.resumescreening.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.resumescreening.dto.ResumeUploadResponseDTO;
import com.resumescreening.model.Resume;
import com.resumescreening.repository.ResumeRepository;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class ResumeServiceTest {

    private ResumeService resumeService;

    @BeforeEach
    void setUp() {
        ResumeRepository resumeRepository = mock(ResumeRepository.class);
        AtomicLong ids = new AtomicLong(1);
        when(resumeRepository.save(any(Resume.class))).thenAnswer(invocation -> {
            Resume resume = invocation.getArgument(0);
            resume.setId(ids.getAndIncrement());
            return resume;
        });
        resumeService = new ResumeService(resumeRepository);
    }

    @Test
    void uploadsFresherResumeWithZeroYearsExperience() {
        ResumeUploadResponseDTO response = uploadTextResume(
                "fresher-resume.txt",
                """
                        Name: Priya Sharma
                        Email: priya@example.com
                        Skills: Java, SQL, React
                        Experience: 0 years
                        Education: B.Tech Computer Science
                        Projects: Student portal, Resume screening dashboard
                        Summary: Fresher with academic projects in Java and React.
                        """
        );

        assertThat(response.uploadedCount()).isEqualTo(1);
        assertThat(response.warnings()).isEmpty();
        assertThat(response.resumes().get(0).experienceYears()).isZero();
    }

    @Test
    void uploadsExperiencedResumeWithDecimalYearsExperience() {
        ResumeUploadResponseDTO response = uploadTextResume(
                "experienced-resume.txt",
                """
                        Name: Neha Rao
                        Email: neha@example.com
                        Skills: Java, Spring Boot, SQL
                        Experience: 3.5 years
                        Education: B.E. Information Technology
                        Projects: HR analytics API, Candidate ranking service
                        Summary: Backend engineer with Spring Boot and SQL experience.
                        """
        );

        assertThat(response.uploadedCount()).isEqualTo(1);
        assertThat(response.warnings()).isEmpty();
        assertThat(response.resumes().get(0).experienceYears()).isEqualTo(3.5);
    }

    @Test
    void defaultsCgpaAndDateHeavyFresherResumeToZeroExperience() {
        ResumeUploadResponseDTO response = uploadTextResume(
                "sanitized-fresher.txt",
                """
                        Ananya Iyer
                        ananya@example.com
                        Skills
                        Python, Java, HTML, CSS
                        Education
                        B.Tech Computer Science 2021-2025, CGPA 8.7/10
                        Projects
                        ATS checker v1.2, portfolio site, internship tracker 2.0
                        Summary
                        Fresher candidate with internships from 2023.06 to 2024.01 and 91.5% in higher secondary.
                        """
        );

        assertThat(response.uploadedCount()).isEqualTo(1);
        assertThat(response.warnings()).isEmpty();
        assertThat(response.resumes().get(0).experienceYears()).isZero();
    }

    @Test
    void multipleDecimalLikeValuesDoNotCreateUploadWarning() {
        ResumeUploadResponseDTO response = uploadTextResume(
                "ats-friendly-resume.txt",
                """
                        Name: Kiran Patel
                        Email: kiran@example.com
                        Phone: +91 98765 43210
                        Skills: Java, React, REST, MySQL
                        Education: B.Tech CSE, CGPA 8.5, graduated 2024
                        Projects: Built API v2.1, improved score from 7.8 to 9.2, released dashboard 1.0.3
                        Summary: ATS-friendly resume with measurable outcomes and clean sections.
                        Role: Software Developer
                        """
        );

        assertThat(response.uploadedCount()).isEqualTo(1);
        assertThat(response.warnings()).isEmpty();
        assertThat(response.resumes().get(0).experienceYears()).isZero();
    }

    @Test
    void parsesCommonAtsSectionAliases() {
        ResumeUploadResponseDTO response = uploadTextResume(
                "technical-skills-resume.txt",
                """
                        Sree Example
                        sree@example.com

                        TECHNICAL SKILLS
                        Java, SQL, Python, Firebase

                        EDUCATION
                        B.Tech Computer Science

                        ACADEMIC PROJECTS
                        Complaint triage system, Resume screening dashboard

                        PROFILE
                        Fresher focused on AI-assisted software projects.
                        """
        );

        assertThat(response.uploadedCount()).isEqualTo(1);
        assertThat(response.warnings()).isEmpty();
        assertThat(response.resumes().get(0).skills()).contains("Java", "SQL", "Python", "Firebase");
        assertThat(response.resumes().get(0).projects()).contains("Complaint triage system");
        assertThat(response.resumes().get(0).summary()).contains("Fresher focused");
    }

    @Test
    void rejectsReadableFileThatDoesNotLookLikeResume() {
        ResumeUploadResponseDTO response = uploadTextResume(
                "quarterly-report.txt",
                """
                        Quarterly revenue report
                        The marketing budget increased this month and the
                        operations team reviewed vendor contracts for office
                        supplies, travel reimbursements, event planning, and
                        monthly subscription renewals. Revenue improved after
                        a campaign refresh, but the document only summarizes
                        sales channels, procurement notes, invoice timing,
                        customer segments, and administrative follow-up items.
                        It intentionally contains enough text to look like a
                        readable PDF document while still not describing a
                        candidate profile, skills, education, projects, or
                        work history.
                        """
        );

        assertThat(response.uploadedCount()).isZero();
        assertThat(response.resumes()).isEmpty();
        assertThat(response.warnings()).hasSize(1);
        assertThat(response.warnings().get(0)).contains("does not look like a valid resume");
    }

    @Test
    void rejectsUnsupportedFileExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "profile-photo.png",
                "image/png",
                "not a resume".getBytes(StandardCharsets.UTF_8)
        );

        ResumeUploadResponseDTO response = resumeService.upload(
                new MockMultipartFile[]{file},
                "Software Developer"
        );

        assertThat(response.uploadedCount()).isZero();
        assertThat(response.resumes()).isEmpty();
        assertThat(response.warnings()).hasSize(1);
        assertThat(response.warnings().get(0)).contains("Only PDF, DOC, DOCX, or TXT files are allowed");
    }

    private ResumeUploadResponseDTO uploadTextResume(String filename, String content) {
        MockMultipartFile file = new MockMultipartFile(
                "files",
                filename,
                "text/plain",
                content.getBytes(StandardCharsets.UTF_8)
        );
        return resumeService.upload(new MockMultipartFile[]{file}, "Software Developer");
    }
}
