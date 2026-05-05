package com.resumescreening.service;

import com.resumescreening.dto.ATSChecksDTO;
import com.resumescreening.dto.ATSScoreBreakdownDTO;
import com.resumescreening.dto.ATSScoringResultDTO;
import com.resumescreening.model.JobRole;
import com.resumescreening.model.Resume;
import com.resumescreening.util.ScreeningStatusResolver;
import com.resumescreening.util.ResumeValidator;
import com.resumescreening.util.StringListConverter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RuleBasedScoringService implements Scorable {

    @Override
    public double calculateScore(Resume resume, JobRole role) {
        return calculateAtsResult(resume, role).score();
    }

    public ATSScoringResultDTO calculateAtsResult(Resume resume, JobRole role) {
        ResumeValidator.validateForScreening(resume);
        double skillScore = weightedRatio(
                StringListConverter.countMatches(role.getRequiredSkills(), resume.getSkills()),
                role.getRequiredSkills().size(),
                role.getSkillWeightage());
        double experienceScore = scoreExperience(resume.getExperienceYears(), role.getMinExperience(), role.getExperienceWeightage());
        double projectScore = scoreProjects(resume.getProjects(), role.getKeywords(), role.getProjectWeightage());
        double educationScore = scoreEducation(resume.getEducation(), role.getRequiredEducation(), role.getEducationWeightage());
        String resumeText = combinedResumeText(resume);
        List<String> matchedKeywords = matchedKeywords(resumeText, role);
        List<String> missingKeywords = missingKeywords(resumeText, role);
        double keywordScore = weightedRatio(matchedKeywords.size(), keywordUniverse(role).size(), role.getKeywordWeightage());
        double score = Math.min(100, round(skillScore + experienceScore + projectScore + educationScore + keywordScore));
        ATSScoreBreakdownDTO breakdown = new ATSScoreBreakdownDTO(
                skillScore,
                experienceScore,
                projectScore,
                educationScore,
                keywordScore);
        ATSChecksDTO checks = new ATSChecksDTO(
                true,
                resumeText.length() >= 80 && resumeText.split("\\s+").length >= 12,
                !resumeText.matches(".*[{}<>|]{2,}.*"),
                hasRequiredSections(resume),
                List.of());
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

    private double scoreProjects(List<String> projects, List<String> keywords, int weightage) {
        if (weightage <= 0 || projects == null || projects.isEmpty()) {
            return 0;
        }
        if (keywords == null || keywords.isEmpty()) {
            return Math.min(weightage, projects.size() * (weightage / 3.0));
        }
        String projectText = String.join(" ", projects);
        long matches = keywords.stream()
                .filter(keyword -> StringListConverter.containsNormalized(projectText, keyword))
                .count();
        return weightedRatio(matches, keywords.size(), weightage);
    }

    private double scoreEducation(String education, String requiredEducation, int weightage) {
        if (weightage <= 0) {
            return 0;
        }
        if (requiredEducation == null || requiredEducation.isBlank()) {
            return weightage;
        }
        if (StringListConverter.isPartialMatch(education, requiredEducation)) {
            return weightage;
        }
        List<String> requiredTokens = List.of(requiredEducation.split("\\s+"));
        long matches = requiredTokens.stream()
                .filter(token -> StringListConverter.containsNormalized(education, token))
                .count();
        return weightedRatio(matches, requiredTokens.size(), weightage);
    }

    private double weightedRatio(long matches, int total, int weightage) {
        if (total <= 0 || weightage <= 0) {
            return 0;
        }
        return round(weightage * Math.min(1, matches / (double) total));
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
                resume.getSummary() == null ? "" : resume.getSummary(),
                resume.getEducation() == null ? "" : resume.getEducation(),
                String.join(" ", resume.getSkills() == null ? List.of() : resume.getSkills()),
                String.join(" ", resume.getProjects() == null ? List.of() : resume.getProjects()),
                resume.getAppliedRole() == null ? "" : resume.getAppliedRole()).toLowerCase();
    }

    private boolean hasRequiredSections(Resume resume) {
        return resume.getSummary() != null && !resume.getSummary().isBlank()
                && resume.getEducation() != null && !resume.getEducation().isBlank()
                && resume.getSkills() != null && !resume.getSkills().isEmpty()
                && resume.getProjects() != null && !resume.getProjects().isEmpty();
    }

    private String buildRemarks(double score, String status, List<String> missingKeywords) {
        if ("SHORTLISTED".equals(status)) {
            return "Strong ATS alignment. Recommended for AI interview.";
        }
        if ("CONSIDER".equals(status)) {
            return "Moderate ATS alignment. Review missing signals: " + String.join(", ", missingKeywords.stream().limit(4).toList());
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
