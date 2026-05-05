package com.resumescreening.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ScreeningReport {

    private Long id;
    private Long resumeId;
    private Long roleId;
    private String candidateName;
    private String roleName;
    private double score;
    private String status;
    private String remarks;
    private List<String> matchedKeywords = new ArrayList<>();
    private List<String> missingKeywords = new ArrayList<>();
    private double skillsScore;
    private double experienceScore;
    private double projectScore;
    private double educationScore;
    private double keywordScore;
    private boolean atsComplete;
    private boolean atsReadable;
    private boolean atsSimpleFormatting;
    private boolean atsRequiredSections;
    private List<String> atsIssues = new ArrayList<>();
    private String explanation;
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public List<String> getMatchedKeywords() {
        return matchedKeywords;
    }

    public void setMatchedKeywords(List<String> matchedKeywords) {
        this.matchedKeywords = matchedKeywords == null ? new ArrayList<>() : new ArrayList<>(matchedKeywords);
    }

    public List<String> getMissingKeywords() {
        return missingKeywords;
    }

    public void setMissingKeywords(List<String> missingKeywords) {
        this.missingKeywords = missingKeywords == null ? new ArrayList<>() : new ArrayList<>(missingKeywords);
    }

    public double getSkillsScore() {
        return skillsScore;
    }

    public void setSkillsScore(double skillsScore) {
        this.skillsScore = skillsScore;
    }

    public double getExperienceScore() {
        return experienceScore;
    }

    public void setExperienceScore(double experienceScore) {
        this.experienceScore = experienceScore;
    }

    public double getProjectScore() {
        return projectScore;
    }

    public void setProjectScore(double projectScore) {
        this.projectScore = projectScore;
    }

    public double getEducationScore() {
        return educationScore;
    }

    public void setEducationScore(double educationScore) {
        this.educationScore = educationScore;
    }

    public double getKeywordScore() {
        return keywordScore;
    }

    public void setKeywordScore(double keywordScore) {
        this.keywordScore = keywordScore;
    }

    public boolean isAtsComplete() {
        return atsComplete;
    }

    public void setAtsComplete(boolean atsComplete) {
        this.atsComplete = atsComplete;
    }

    public boolean isAtsReadable() {
        return atsReadable;
    }

    public void setAtsReadable(boolean atsReadable) {
        this.atsReadable = atsReadable;
    }

    public boolean isAtsSimpleFormatting() {
        return atsSimpleFormatting;
    }

    public void setAtsSimpleFormatting(boolean atsSimpleFormatting) {
        this.atsSimpleFormatting = atsSimpleFormatting;
    }

    public boolean isAtsRequiredSections() {
        return atsRequiredSections;
    }

    public void setAtsRequiredSections(boolean atsRequiredSections) {
        this.atsRequiredSections = atsRequiredSections;
    }

    public List<String> getAtsIssues() {
        return atsIssues;
    }

    public void setAtsIssues(List<String> atsIssues) {
        this.atsIssues = atsIssues == null ? new ArrayList<>() : new ArrayList<>(atsIssues);
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
