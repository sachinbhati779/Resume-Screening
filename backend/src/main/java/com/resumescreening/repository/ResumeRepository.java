package com.resumescreening.repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.resumescreening.exception.CandidateNotFoundException;
import com.resumescreening.exception.DatabaseOperationException;
import com.resumescreening.model.Resume;
import com.resumescreening.util.StringListConverter;

@Repository
public class ResumeRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Resume> rowMapper = (rs, rowNum) -> {
        Resume resume = new Resume();
        resume.setId(rs.getLong("id"));
        resume.setCandidateName(rs.getString("candidate_name"));
        resume.setEmail(rs.getString("email"));
        resume.setPhone(rs.getString("phone"));
        resume.setSkills(StringListConverter.fromCsv(rs.getString("skills")));
        resume.setExperienceYears(rs.getDouble("experience_years"));
        resume.setEducation(rs.getString("education"));
        resume.setProjects(StringListConverter.fromCsv(rs.getString("projects")));
        resume.setSummary(rs.getString("summary"));
        resume.setAppliedRole(rs.getString("applied_role"));
        resume.setFileName(rs.getString("file_name"));
        resume.setFileType(rs.getString("file_type"));
        long fileSize = rs.getLong("file_size");
        resume.setFileSize(rs.wasNull() ? null : fileSize);
        resume.setFileData(rs.getBytes("file_data"));
        resume.setExtractedText(rs.getString("extracted_text"));
        resume.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        resume.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return resume;
    };

    public ResumeRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Resume save(Resume resume) {
        String sql = """
                INSERT INTO resumes (
                    candidate_name, email, phone, skills, experience_years,
                    education, projects, summary, applied_role,
                    file_name, file_type, file_size, file_data, extracted_text
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, resume.getCandidateName());
                ps.setString(2, resume.getEmail());
                ps.setString(3, resume.getPhone());
                ps.setString(4, StringListConverter.toCsv(resume.getSkills()));
                ps.setDouble(5, resume.getExperienceYears());
                ps.setString(6, resume.getEducation());
                ps.setString(7, StringListConverter.toCsv(resume.getProjects()));
                ps.setString(8, resume.getSummary());
                ps.setString(9, resume.getAppliedRole());
                ps.setString(10, resume.getFileName());
                ps.setString(11, resume.getFileType());
                if (resume.getFileSize() == null) {
                    ps.setObject(12, null);
                } else {
                    ps.setLong(12, resume.getFileSize());
                }
                ps.setBytes(13, resume.getFileData());
                ps.setString(14, resume.getExtractedText());
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key == null) {
                throw new CandidateNotFoundException("Failed to create resume");
            }
            return findById(key.longValue()).orElseThrow(() -> new CandidateNotFoundException("Created resume not found"));
        } catch (DataAccessException exception) {
            throw new DatabaseOperationException("Unable to save resume", exception);
        }
    }

    public List<Resume> findAll() {
        return jdbcTemplate.query("SELECT * FROM resumes ORDER BY created_at DESC", rowMapper);
    }

    public List<Resume> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return findAll();
        }
        String placeholders = ids.stream().map(id -> "?").reduce((left, right) -> left + "," + right).orElse("?");
        String sql = "SELECT * FROM resumes WHERE id IN (" + placeholders + ") ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, rowMapper, ids.toArray());
    }

    public Optional<Resume> findById(Long id) {
        List<Resume> resumes = jdbcTemplate.query("SELECT * FROM resumes WHERE id = ?", rowMapper, id);
        return resumes.stream().findFirst();
    }

    public long count() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM resumes", Long.class);
        return count == null ? 0 : count;
    }
}
