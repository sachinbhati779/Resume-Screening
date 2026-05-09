package com.resumescreening.util;

import com.resumescreening.exception.IncompleteResumeException;
import com.resumescreening.model.Resume;
import java.util.List;
import java.util.Locale;

public final class ResumeValidator {

    private static final int MIN_RESUME_TEXT_LENGTH = 250;
    private static final int MIN_RESUME_SIGNALS = 3;
    private static final List<String> SKILL_SECTION_HEADERS = List.of(
            "skills",
            "technical skills",
            "key skills",
            "core skills",
            "tools & platforms",
            "programming"
    );
    private static final List<String> EDUCATION_SECTION_HEADERS = List.of(
            "education",
            "academic education",
            "academics"
    );
    private static final List<String> EDUCATION_KEYWORDS = List.of(
            "b.tech",
            "bachelor",
            "degree",
            "university",
            "college"
    );
    private static final List<String> PROJECT_SECTION_HEADERS = List.of(
            "projects",
            "academic projects",
            "project experience"
    );
    private static final List<String> SUMMARY_SECTION_HEADERS = List.of(
            "summary",
            "profile",
            "objective",
            "career objective"
    );
    private static final List<String> EXPERIENCE_SECTION_HEADERS = List.of(
            "experience",
            "work experience",
            "professional experience",
            "employment",
            "internship",
            "internships"
    );

    private ResumeValidator() {
    }

    public static void validateForScreening(Resume resume) {
        validateLikelyResume(resume);
    }

    public static void validateLikelyResume(Resume resume) {
        if (resume == null) {
            throw new IncompleteResumeException("Resume is required");
        }
        String text = searchableText(resume);
        if (text.isBlank()) {
            throw new IncompleteResumeException("Resume has no searchable content for screening");
        }
        int signals = resumeSignalCount(resume, text);
        if (text.length() < MIN_RESUME_TEXT_LENGTH || signals < MIN_RESUME_SIGNALS) {
            throw new IncompleteResumeException("Uploaded file does not look like a valid resume");
        }
    }

    private static int resumeSignalCount(Resume resume, String text) {
        int signals = 0;

        if (hasText(resume.getEmail())) {
            signals++;
        }
        if (hasText(resume.getPhone())) {
            signals++;
        }
        if (resume.getSkills() != null && !resume.getSkills().isEmpty()
                || containsSectionHeading(text, SKILL_SECTION_HEADERS)) {
            signals++;
        }
        if (resume.getProjects() != null && !resume.getProjects().isEmpty()
                || containsSectionHeading(text, PROJECT_SECTION_HEADERS)) {
            signals++;
        }
        if (hasText(resume.getEducation())
                || containsSectionHeading(text, EDUCATION_SECTION_HEADERS)
                || containsAny(text, EDUCATION_KEYWORDS)) {
            signals++;
        }
        if (containsSectionHeading(text, SUMMARY_SECTION_HEADERS)) {
            signals++;
        }
        if (resume.getExperienceYears() > 0 || containsSectionHeading(text, EXPERIENCE_SECTION_HEADERS)) {
            signals++;
        }

        return signals;
    }

    private static String searchableText(Resume resume) {
        return String.join(" ",
                safeText(resume.getExtractedText()),
                safeText(resume.getSummary()),
                safeText(resume.getEducation()),
                String.join(" ", resume.getSkills() == null ? List.of() : resume.getSkills()),
                String.join(" ", resume.getProjects() == null ? List.of() : resume.getProjects()),
                safeText(resume.getAppliedRole())
        ).trim();
    }

    private static boolean containsAny(String text, List<String> signals) {
        String normalized = text.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        return signals.stream().anyMatch(normalized::contains);
    }

    private static boolean containsSectionHeading(String text, List<String> headings) {
        for (String line : text.split("\\R")) {
            String normalizedLine = line.toLowerCase(Locale.ROOT)
                    .replaceAll("^[\\s•*-]+", "")
                    .trim();
            for (String heading : headings) {
                if (normalizedLine.equals(heading)
                        || normalizedLine.startsWith(heading + ":")
                        || normalizedLine.startsWith(heading + " -")
                        || normalizedLine.startsWith(heading + " |")) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String safeText(String value) {
        return value == null ? "" : value;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
