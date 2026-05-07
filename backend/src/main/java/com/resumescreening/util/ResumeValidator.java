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
        boolean hasStructuredContent = hasText(resume.getSummary())
                || hasText(resume.getEducation())
                || resume.getSkills() != null && !resume.getSkills().isEmpty()
                || resume.getProjects() != null && !resume.getProjects().isEmpty();
        boolean hasExtractedText = hasText(resume.getExtractedText());
        if (!hasStructuredContent && !hasExtractedText) {
            throw new IncompleteResumeException("Resume has no searchable content for screening");
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
