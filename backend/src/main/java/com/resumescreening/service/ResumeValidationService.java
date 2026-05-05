package com.resumescreening.service;

import com.resumescreening.dto.ATSChecksDTO;
import com.resumescreening.exception.IncompleteResumeException;
import com.resumescreening.model.Resume;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ResumeValidationService {

    public ATSChecksDTO checkAtsReadiness(Resume resume) {
        List<String> issues = new ArrayList<>();

        if (resume == null) {
            issues.add("Resume payload is missing.");
            return new ATSChecksDTO(false, false, false, false, issues);
        }

        addIfBlank(issues, resume.getCandidateName(), "Candidate name is required.");
        addIfBlank(issues, resume.getEmail(), "Email is required.");
        if (resume.getSkills() == null || resume.getSkills().isEmpty()) {
            issues.add("At least one skill is required.");
        }
        addIfBlank(issues, resume.getEducation(), "Education is required.");
        addIfBlank(issues, resume.getSummary(), "Professional summary is required.");
        if (!hasProjectsOrRelevantWork(resume)) {
            issues.add("Projects or relevant work details are required.");
        }

        String combinedText = combinedText(resume);
        boolean readable = combinedText.length() >= 80 && wordCount(combinedText) >= 12;
        if (!readable) {
            issues.add("Resume content is too short for reliable ATS screening.");
        }

        boolean simpleFormatting = !combinedText.matches(".*[{}<>|]{2,}.*")
                && !combinedText.toLowerCase().contains("image-only")
                && !combinedText.toLowerCase().contains("scanned");
        if (!simpleFormatting) {
            issues.add("Resume appears to use complex or scanned formatting.");
        }

        boolean hasRequiredSections = hasText(resume.getSummary())
                && hasText(resume.getEducation())
                && resume.getSkills() != null
                && !resume.getSkills().isEmpty()
                && hasProjectsOrRelevantWork(resume);
        boolean complete = issues.stream().noneMatch(issue -> issue.contains("required"));

        return new ATSChecksDTO(complete, readable, simpleFormatting, hasRequiredSections, issues);
    }

    public void validateForAtsScreening(Resume resume) {
        ATSChecksDTO checks = checkAtsReadiness(resume);
        if (!checks.complete() || !checks.hasRequiredSections()) {
            throw new IncompleteResumeException(String.join(" ", checks.issues()));
        }
    }

    private boolean hasProjectsOrRelevantWork(Resume resume) {
        if (resume.getProjects() != null && !resume.getProjects().isEmpty()) {
            return true;
        }
        String summary = resume.getSummary() == null ? "" : resume.getSummary().toLowerCase();
        return summary.contains("project")
                || summary.contains("internship")
                || summary.contains("work")
                || summary.contains("company")
                || summary.contains("experience");
    }

    private void addIfBlank(List<String> issues, String value, String issue) {
        if (!hasText(value)) {
            issues.add(issue);
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String combinedText(Resume resume) {
        return String.join(" ",
                safe(resume.getCandidateName()),
                safe(resume.getEmail()),
                String.join(" ", resume.getSkills() == null ? List.of() : resume.getSkills()),
                safe(resume.getEducation()),
                String.join(" ", resume.getProjects() == null ? List.of() : resume.getProjects()),
                safe(resume.getSummary()),
                safe(resume.getAppliedRole()));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private int wordCount(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }
        return value.trim().split("\\s+").length;
    }
}
