package com.resumescreening.model;

import java.util.ArrayList;
import java.util.List;

public class FresherCandidate extends Candidate {

    private String internshipDetails;
    private List<String> certifications = new ArrayList<>();

    public String getInternshipDetails() {
        return internshipDetails;
    }

    public void setInternshipDetails(String internshipDetails) {
        this.internshipDetails = internshipDetails;
    }

    public List<String> getCertifications() {
        return certifications;
    }

    public void setCertifications(List<String> certifications) {
        this.certifications = certifications == null ? new ArrayList<>() : new ArrayList<>(certifications);
    }
}
