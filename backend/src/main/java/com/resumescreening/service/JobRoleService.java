package com.resumescreening.service;

import com.resumescreening.dto.JobRoleRequestDTO;
import com.resumescreening.dto.JobRoleResponseDTO;
import com.resumescreening.exception.InvalidJobRoleException;
import com.resumescreening.model.JobRole;
import com.resumescreening.repository.JobRoleRepository;
import com.resumescreening.util.ScreeningConstants;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JobRoleService {

    private static final Logger log = LoggerFactory.getLogger(JobRoleService.class);

    private final JobRoleRepository jobRoleRepository;

    public JobRoleService(JobRoleRepository jobRoleRepository) {
        this.jobRoleRepository = jobRoleRepository;
    }

    public JobRoleResponseDTO create(JobRoleRequestDTO request) {
        JobRole role = toModel(request);
        validate(role);
        JobRole saved = jobRoleRepository.save(role);
        log.info("Created job role id={} roleName={}", saved.getId(), saved.getRoleName());
        return toResponse(saved);
    }

    public List<JobRoleResponseDTO> findAll() {
        ensureDefaultRoleExists();
        return jobRoleRepository.findAll().stream().map(this::toResponse).toList();
    }

    public JobRole findModelById(Long id) {
        return jobRoleRepository.findById(id)
                .orElseThrow(() -> new InvalidJobRoleException("Job role not found: " + id));
    }

    public JobRoleResponseDTO findById(Long id) {
        return toResponse(findModelById(id));
    }

    private JobRole toModel(JobRoleRequestDTO request) {
        JobRole role = new JobRole();
        role.setRoleName(request.roleName());
        role.setRequiredSkills(request.requiredSkills());
        role.setMinExperience(request.minExperience());
        role.setRequiredEducation(request.requiredEducation());
        role.setKeywords(request.keywords());
        role.setSkillWeightage(valueOrDefault(request.skillWeightage(), ScreeningConstants.DEFAULT_SKILL_WEIGHT));
        role.setExperienceWeightage(valueOrDefault(request.experienceWeightage(), ScreeningConstants.DEFAULT_EXPERIENCE_WEIGHT));
        role.setProjectWeightage(valueOrDefault(request.projectWeightage(), ScreeningConstants.DEFAULT_PROJECT_WEIGHT));
        role.setEducationWeightage(valueOrDefault(request.educationWeightage(), ScreeningConstants.DEFAULT_EDUCATION_WEIGHT));
        role.setKeywordWeightage(valueOrDefault(request.keywordWeightage(), ScreeningConstants.DEFAULT_KEYWORD_WEIGHT));
        return role;
    }

    private int valueOrDefault(Integer value, int defaultValue) {
        return value == null ? defaultValue : value;
    }

    private void validate(JobRole role) {
        if (role.getRoleName() == null || role.getRoleName().isBlank()) {
            throw new InvalidJobRoleException("Role name is required");
        }
        if (role.getRequiredSkills() == null || role.getRequiredSkills().isEmpty()) {
            throw new InvalidJobRoleException("Required skills are required");
        }
        int totalWeight = role.getSkillWeightage()
                + role.getExperienceWeightage()
                + role.getProjectWeightage()
                + role.getEducationWeightage()
                + role.getKeywordWeightage();
        if (totalWeight <= 0) {
            throw new InvalidJobRoleException("At least one scoring weight must be greater than zero");
        }
    }

    private void ensureDefaultRoleExists() {
        if (jobRoleRepository.count() > 0) {
            return;
        }
        JobRole role = new JobRole();
        role.setRoleName("General Software Engineer");
        role.setRequiredSkills(List.of("Java", "SQL", "React", "REST"));
        role.setMinExperience(0);
        role.setRequiredEducation("Computer Science");
        role.setKeywords(List.of("java", "sql", "react", "rest", "api", "project", "database"));
        role.setSkillWeightage(ScreeningConstants.DEFAULT_SKILL_WEIGHT);
        role.setExperienceWeightage(ScreeningConstants.DEFAULT_EXPERIENCE_WEIGHT);
        role.setProjectWeightage(ScreeningConstants.DEFAULT_PROJECT_WEIGHT);
        role.setEducationWeightage(ScreeningConstants.DEFAULT_EDUCATION_WEIGHT);
        role.setKeywordWeightage(ScreeningConstants.DEFAULT_KEYWORD_WEIGHT);
        JobRole saved = jobRoleRepository.save(role);
        log.info("Created default job role id={} roleName={}", saved.getId(), saved.getRoleName());
    }

    public JobRoleResponseDTO toResponse(JobRole role) {
        return new JobRoleResponseDTO(
                role.getId(),
                role.getRoleName(),
                role.getRequiredSkills(),
                role.getMinExperience(),
                role.getRequiredEducation(),
                role.getKeywords(),
                role.getSkillWeightage(),
                role.getExperienceWeightage(),
                role.getProjectWeightage(),
                role.getEducationWeightage(),
                role.getKeywordWeightage(),
                role.getCreatedAt()
        );
    }
}
