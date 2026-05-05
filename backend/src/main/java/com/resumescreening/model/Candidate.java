package com.resumescreening.model;

import java.util.ArrayList;
import java.util.List;

public class Candidate {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private List<String> skills = new ArrayList<>();
    private double experienceYears;
    private String education;
    private List<String> projects = new ArrayList<>();
    private String summary;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills == null ? new ArrayList<>() : new ArrayList<>(skills);
    }

    public double getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(double experienceYears) {
        this.experienceYears = experienceYears;
    }

    public String getEducation() {
        return education;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public List<String> getProjects() {
        return projects;
    }

    public void setProjects(List<String> projects) {
        this.projects = projects == null ? new ArrayList<>() : new ArrayList<>(projects);
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
