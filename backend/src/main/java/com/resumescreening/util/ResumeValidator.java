package com.resumescreening.util;

import com.resumescreening.exception.IncompleteResumeException;
import com.resumescreening.model.Resume;

public final class ResumeValidator {

    private ResumeValidator() {
    }

    public static void validateForScreening(Resume resume) {
        if (resume == null) {
            throw new IncompleteResumeException("Resume is required");
        }
        requireText(resume.getCandidateName(), "Candidate name is required");
        requireText(resume.getEmail(), "Email is required");
        if (resume.getSkills() == null || resume.getSkills().isEmpty()) {
            throw new IncompleteResumeException("At least one skill is required");
        }
        requireText(resume.getSummary(), "Summary is required");
        requireText(resume.getEducation(), "Education is required");
    }

    private static void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IncompleteResumeException(message);
        }
    }
}
