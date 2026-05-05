package com.resumescreening.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.resumescreening.model.JobRole;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class JobRoleRepositoryTest {

    private JobRoleRepository repository;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:job_role_repo;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
                "sa",
                "");
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS job_roles (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    role_name VARCHAR(160) NOT NULL,
                    required_skills TEXT NOT NULL,
                    min_experience DECIMAL(5,2) NOT NULL DEFAULT 0,
                    required_education VARCHAR(255) NOT NULL,
                    keywords TEXT,
                    skill_weightage INT NOT NULL DEFAULT 40,
                    experience_weightage INT NOT NULL DEFAULT 25,
                    project_weightage INT NOT NULL DEFAULT 15,
                    education_weightage INT NOT NULL DEFAULT 10,
                    keyword_weightage INT NOT NULL DEFAULT 10,
                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);
        jdbcTemplate.update("DELETE FROM job_roles");
        repository = new JobRoleRepository(jdbcTemplate);
    }

    @Test
    void insertsAndFetchesJobRole() {
        JobRole role = new JobRole();
        role.setRoleName("Backend Engineer");
        role.setRequiredSkills(List.of("Java", "SQL"));
        role.setMinExperience(3);
        role.setRequiredEducation("Computer Science");
        role.setKeywords(List.of("rest", "jdbc"));

        JobRole saved = repository.save(role);

        assertThat(saved.getId()).isNotNull();
        assertThat(repository.findById(saved.getId())).isPresent();
        assertThat(repository.findAll()).hasSize(1);
    }
}
