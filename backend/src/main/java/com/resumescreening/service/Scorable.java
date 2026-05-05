package com.resumescreening.service;

import com.resumescreening.model.JobRole;
import com.resumescreening.model.Resume;

public interface Scorable {

    double calculateScore(Resume resume, JobRole role);
}
