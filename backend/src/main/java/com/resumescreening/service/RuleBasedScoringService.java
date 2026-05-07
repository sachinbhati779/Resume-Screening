package com.resumescreening.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.resumescreening.dto.ATSChecksDTO;
import com.resumescreening.dto.ATSScoreBreakdownDTO;
import com.resumescreening.dto.ATSScoringResultDTO;
import com.resumescreening.model.JobRole;
import com.resumescreening.model.Resume;
import com.resumescreening.util.ResumeTextSimilarity;
import com.resumescreening.util.ResumeValidator;
import com.resumescreening.util.ScreeningStatusResolver;
import com.resumescreening.util.StringListConverter;

@Service
public class RuleBasedScoringService implements Scorable {

    @Override
    public double calculateScore(Resume resume, JobRole role) {
        return calculateAtsResult(resume, role).score();
    }

    public ATSScoringResultDTO calculateAtsResult(Resume resume, JobRole role) {
        ResumeValidator.validateForScreening(resume);
        String resumeText = combinedResumeText(resume);
        List<String> matchedSkills = matchedSkills(resumeText, resume.getSkills(), role.getRequiredSkills());
        double skillScore = weightedRatio(
                matchedSkills.size(),
                role.getRequiredSkills().size(),
                role.getSkillWeightage());
        double experienceScore = scoreExperience(resume.getExperienceYears(), role.getMinExperience(), role.getExperienceWeightage());
        double projectScore = scoreProjects(resumeText, resume.getProjects(), role.getKeywords(), role.getProjectWeightage());
        double educationScore = scoreEducation(resumeText, resume.getEducation(), role.getRequiredEducation(), role.getEducationWeightage());
        List<String> matchedKeywords = matchedKeywords(resumeText, role);
        List<String> missingKeywords = missingKeywords(resumeText, role);
        double keywordScore = scoreKeywordAlignment(resumeText, role, matchedKeywords.size());
        double score = Math.min(100, round(skillScore + experienceScore + projectScore + educationScore + keywordScore));
        ATSScoreBreakdownDTO breakdown = new ATSScoreBreakdownDTO(
                skillScore,
                experienceScore,
                projectScore,
                educationScore,
                keywordScore);
        ATSChecksDTO checks = new ATSChecksDTO(
                isComplete(resume),
                resumeText.length() >= 80 && resumeText.split("\\s+").length >= 12,
                !resumeText.matches(".*[{}<>|]{2,}.*"),
                hasRequiredSections(resume),
                atsIssues(resume, resumeText, role));
        String status = ScreeningStatusResolver.resolve(score);

        return new ATSScoringResultDTO(
                score,
                status,
                buildRemarks(score, status, missingKeywords),
                matchedKeywords,
                missingKeywords,
                breakdown,
                checks,
                buildExplanation(breakdown, matchedKeywords, missingKeywords));
    }

    private double scoreExperience(double candidateExperience, double minExperience, int weightage) {
        if (weightage <= 0) {
            return 0;
        }
        if (minExperience <= 0) {
            return weightage;
        }
        double ratio = Math.min(1, candidateExperience / minExperience);
        return round(weightage * ratio);
    }

    private double scoreProjects(String resumeText, List<String> projects, List<String> keywords, int weightage) {
        if (weightage <= 0) {
            return 0;
        }
        String projectText = String.join(" ",
                String.join(" ", projects == null ? List.of() : projects),
                resumeText == null ? "" : resumeText);
        if (keywords == null || keywords.isEmpty()) {
            return projectText.isBlank() ? 0 : Math.min(weightage, weightage / 2.0);
        }
        long matches = keywords.stream()
                .filter(keyword -> StringListConverter.containsNormalized(projectText, keyword))
                .count();
        return weightedRatio(matches, keywords.size(), weightage);
    }

    private double scoreEducation(String resumeText, String education, String requiredEducation, int weightage) {
        if (weightage <= 0) {
            return 0;
        }
        if (requiredEducation == null || requiredEducation.isBlank()) {
            return weightage;
        }
        if (StringListConverter.isPartialMatch(education, requiredEducation)) {
            return weightage;
        }
        if (StringListConverter.containsNormalized(resumeText, requiredEducation)) {
            return weightage;
        }
        List<String> requiredTokens = List.of(requiredEducation.split("\\s+"));
        long matches = requiredTokens.stream()
                .filter(token -> StringListConverter.containsNormalized(education, token)
                        || StringListConverter.containsNormalized(resumeText, token))
                .count();
        return weightedRatio(matches, requiredTokens.size(), weightage);
    }

    private double weightedRatio(long matches, int total, int weightage) {
        if (total <= 0 || weightage <= 0) {
            return 0;
        }
        return round(weightage * Math.min(1, matches / (double) total));
    }

    private double scoreKeywordAlignment(String resumeText, JobRole role, int exactMatches) {
        if (role.getKeywordWeightage() <= 0) {
            return 0;
        }
        List<String> keywords = keywordUniverse(role);
        double exactRatio = keywords.isEmpty() ? 0 : Math.min(1, exactMatches / (double) keywords.size());
        double contentSimilarity = ResumeTextSimilarity.cosineSimilarity(roleProfile(role), resumeText);
        double blendedRatio = Math.min(1, Math.max(exactRatio, (exactRatio * 0.7) + (contentSimilarity * 0.3)));
        return round(role.getKeywordWeightage() * blendedRatio);
    }

    private List<String> matchedKeywords(String resumeText, JobRole role) {
        return keywordUniverse(role).stream()
                .filter(keyword -> StringListConverter.containsNormalized(resumeText, keyword))
                .toList();
    }

    private List<String> missingKeywords(String resumeText, JobRole role) {
        return keywordUniverse(role).stream()
                .filter(keyword -> !StringListConverter.containsNormalized(resumeText, keyword))
                .toList();
    }

    private List<String> matchedSkills(String resumeText, List<String> resumeSkills, List<String> requiredSkills) {
        if (requiredSkills == null || requiredSkills.isEmpty()) {
            return List.of();
        }
        return requiredSkills.stream()
                .filter(requiredSkill -> isSkillPresent(resumeText, resumeSkills, requiredSkill))
                .map(StringListConverter::normalize)
                .distinct()
                .toList();
    }

    private boolean isSkillPresent(String resumeText, List<String> resumeSkills, String requiredSkill) {
        boolean structuredMatch = resumeSkills != null
                && resumeSkills.stream().anyMatch(skill -> StringListConverter.isPartialMatch(skill, requiredSkill));
        return structuredMatch || StringListConverter.containsNormalized(resumeText, requiredSkill);
    }

    private List<String> keywordUniverse(JobRole role) {
        List<String> keywords = new ArrayList<>();
        if (role.getKeywords() != null) {
            keywords.addAll(role.getKeywords());
        }
        if (role.getRequiredSkills() != null) {
            keywords.addAll(role.getRequiredSkills());
        }
        return keywords.stream()
                .map(StringListConverter::normalize)
                .filter(keyword -> !keyword.isBlank())
                .distinct()
                .toList();
    }

    private String combinedResumeText(Resume resume) {
        return String.join(" ",
                resume.getExtractedText() == null ? "" : resume.getExtractedText(),
                resume.getSummary() == null ? "" : resume.getSummary(),
                resume.getEducation() == null ? "" : resume.getEducation(),
                String.join(" ", resume.getSkills() == null ? List.of() : resume.getSkills()),
                String.join(" ", resume.getProjects() == null ? List.of() : resume.getProjects()),
                resume.getAppliedRole() == null ? "" : resume.getAppliedRole()).toLowerCase();
    }

    private String roleProfile(JobRole role) {
        return String.join(" ",
                role.getRoleName() == null ? "" : role.getRoleName(),
                role.getRequiredEducation() == null ? "" : role.getRequiredEducation(),
                String.join(" ", role.getRequiredSkills() == null ? List.of() : role.getRequiredSkills()),
                String.join(" ", role.getKeywords() == null ? List.of() : role.getKeywords()));
    }

    private boolean hasRequiredSections(Resume resume) {
        boolean hasExtractedText = resume.getExtractedText() != null && !resume.getExtractedText().isBlank();
        return (hasExtractedText || resume.getSummary() != null && !resume.getSummary().isBlank())
                && (hasExtractedText || resume.getEducation() != null && !resume.getEducation().isBlank())
                && (hasExtractedText || resume.getSkills() != null && !resume.getSkills().isEmpty())
                && (hasExtractedText || resume.getProjects() != null && !resume.getProjects().isEmpty());
    }

    private boolean isComplete(Resume resume) {
        return hasText(resume.getCandidateName())
                && hasText(resume.getEmail())
                && (hasText(resume.getExtractedText()) || resume.getSkills() != null && !resume.getSkills().isEmpty())
                && (hasText(resume.getSummary()) || hasText(resume.getExtractedText()))
                && (hasText(resume.getEducation()) || hasText(resume.getExtractedText()));
    }

    private List<String> atsIssues(Resume resume, String resumeText, JobRole role) {
        List<String> issues = new ArrayList<>();
        if (!hasText(resume.getCandidateName())) {
            issues.add("Candidate name was missing; upload parser may need a clearer name line.");
        }
        if (!hasText(resume.getEmail())) {
            issues.add("Email is missing.");
        }
        if (!hasText(resume.getSummary()) && !hasText(resume.getExtractedText())) {
            issues.add("Summary or searchable resume text is missing.");
        }
        if (!hasText(resume.getEducation()) && !StringListConverter.containsNormalized(resumeText, role.getRequiredEducation())) {
            issues.add("Education section is missing or does not match the role.");
        }
        if (matchedSkills(resumeText, resume.getSkills(), role.getRequiredSkills()).isEmpty()) {
            issues.add("No required skills were found in the resume text.");
        }
        return issues;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String buildRemarks(double score, String status, List<String> missingKeywords) {
        if ("SHORTLISTED".equals(status)) {
            return "Strong ATS alignment. Recommended for AI interview.";
        }
        if ("CONSIDER".equals(status)) {
            String missing = String.join(", ", missingKeywords.stream().limit(4).map(item -> item.toLowerCase(Locale.ROOT)).toList());
            return missing.isBlank()
                    ? "Moderate ATS alignment. Review profile quality before interview."
                    : "Moderate ATS alignment. Review missing signals: " + missing;
        }
        return "Insufficient ATS alignment for this role.";
    }

    private String buildExplanation(ATSScoreBreakdownDTO breakdown, List<String> matchedKeywords, List<String> missingKeywords) {
        return "Score uses weighted ATS rules: skills "
                + breakdown.skillsScore()
                + ", experience "
                + breakdown.experienceScore()
                + ", projects "
                + breakdown.projectScore()
                + ", education "
                + breakdown.educationScore()
                + ", keywords "
                + breakdown.keywordScore()
                + ". Matched "
                + matchedKeywords.size()
                + " keywords and missed "
                + missingKeywords.size()
                + ".";
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
