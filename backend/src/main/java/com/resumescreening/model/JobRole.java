package com.resumescreening.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JobRole {

    private Long id;
    private String roleName;
    private List<String> requiredSkills = new ArrayList<>();
    private double minExperience;
    private String requiredEducation;
    private List<String> keywords = new ArrayList<>();
    private int skillWeightage = 40;
    private int experienceWeightage = 25;
    private int projectWeightage = 15;
    private int educationWeightage = 10;
    private int keywordWeightage = 10;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public List<String> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<String> requiredSkills) {
        this.requiredSkills = requiredSkills == null ? new ArrayList<>() : new ArrayList<>(requiredSkills);
    }

    public double getMinExperience() {
        return minExperience;
    }

    public void setMinExperience(double minExperience) {
        this.minExperience = minExperience;
    }

    public String getRequiredEducation() {
        return requiredEducation;
    }

    public void setRequiredEducation(String requiredEducation) {
        this.requiredEducation = requiredEducation;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords == null ? new ArrayList<>() : new ArrayList<>(keywords);
    }

    public int getSkillWeightage() {
        return skillWeightage;
    }

    public void setSkillWeightage(int skillWeightage) {
        this.skillWeightage = skillWeightage;
    }

    public int getExperienceWeightage() {
        return experienceWeightage;
    }

    public void setExperienceWeightage(int experienceWeightage) {
        this.experienceWeightage = experienceWeightage;
    }

    public int getProjectWeightage() {
        return projectWeightage;
    }

    public void setProjectWeightage(int projectWeightage) {
        this.projectWeightage = projectWeightage;
    }

    public int getEducationWeightage() {
        return educationWeightage;
    }

    public void setEducationWeightage(int educationWeightage) {
        this.educationWeightage = educationWeightage;
    }

    public int getKeywordWeightage() {
        return keywordWeightage;
    }

    public void setKeywordWeightage(int keywordWeightage) {
        this.keywordWeightage = keywordWeightage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
