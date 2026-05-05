package com.resumescreening.repository;

import com.resumescreening.exception.DatabaseOperationException;
import com.resumescreening.exception.InvalidJobRoleException;
import com.resumescreening.model.JobRole;
import com.resumescreening.util.StringListConverter;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class JobRoleRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<JobRole> rowMapper = (rs, rowNum) -> {
        JobRole role = new JobRole();
        role.setId(rs.getLong("id"));
        role.setRoleName(rs.getString("role_name"));
        role.setRequiredSkills(StringListConverter.fromCsv(rs.getString("required_skills")));
        role.setMinExperience(rs.getDouble("min_experience"));
        role.setRequiredEducation(rs.getString("required_education"));
        role.setKeywords(StringListConverter.fromCsv(rs.getString("keywords")));
        role.setSkillWeightage(rs.getInt("skill_weightage"));
        role.setExperienceWeightage(rs.getInt("experience_weightage"));
        role.setProjectWeightage(rs.getInt("project_weightage"));
        role.setEducationWeightage(rs.getInt("education_weightage"));
        role.setKeywordWeightage(rs.getInt("keyword_weightage"));
        role.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        role.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return role;
    };

    public JobRoleRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public JobRole save(JobRole role) {
        String sql = """
                INSERT INTO job_roles (
                    role_name, required_skills, min_experience, required_education, keywords,
                    skill_weightage, experience_weightage, project_weightage, education_weightage, keyword_weightage
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, role.getRoleName());
                ps.setString(2, StringListConverter.toCsv(role.getRequiredSkills()));
                ps.setDouble(3, role.getMinExperience());
                ps.setString(4, role.getRequiredEducation());
                ps.setString(5, StringListConverter.toCsv(role.getKeywords()));
                ps.setInt(6, role.getSkillWeightage());
                ps.setInt(7, role.getExperienceWeightage());
                ps.setInt(8, role.getProjectWeightage());
                ps.setInt(9, role.getEducationWeightage());
                ps.setInt(10, role.getKeywordWeightage());
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key == null) {
                throw new InvalidJobRoleException("Failed to create job role");
            }
            return findById(key.longValue()).orElseThrow(() -> new InvalidJobRoleException("Created job role not found"));
        } catch (DataAccessException exception) {
            throw new DatabaseOperationException("Unable to save job role", exception);
        }
    }

    public List<JobRole> findAll() {
        return jdbcTemplate.query("SELECT * FROM job_roles ORDER BY created_at DESC", rowMapper);
    }

    public Optional<JobRole> findById(Long id) {
        List<JobRole> roles = jdbcTemplate.query("SELECT * FROM job_roles WHERE id = ?", rowMapper, id);
        return roles.stream().findFirst();
    }
}
