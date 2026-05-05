package com.resumescreening.model;

public class ExperiencedCandidate extends Candidate {

    private String companyName;
    private String currentRole;
    private String previousJobDetails;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCurrentRole() {
        return currentRole;
    }

    public void setCurrentRole(String currentRole) {
        this.currentRole = currentRole;
    }

    public String getPreviousJobDetails() {
        return previousJobDetails;
    }

    public void setPreviousJobDetails(String previousJobDetails) {
        this.previousJobDetails = previousJobDetails;
    }
}
