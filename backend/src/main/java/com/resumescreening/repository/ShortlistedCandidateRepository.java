package com.resumescreening.repository;

import com.resumescreening.exception.DatabaseOperationException;
import com.resumescreening.model.Resume;
import com.resumescreening.model.ScreeningReport;
import com.resumescreening.model.ShortlistedCandidate;
import java.sql.PreparedStatement;
import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class ShortlistedCandidateRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<ShortlistedCandidate> rowMapper = (rs, rowNum) -> {
        ShortlistedCandidate candidate = new ShortlistedCandidate();
        candidate.setId(rs.getLong("id"));
        candidate.setResumeId(rs.getLong("resume_id"));
        candidate.setReportId(rs.getLong("report_id"));
        candidate.setCandidateName(rs.getString("candidate_name"));
        candidate.setEmail(rs.getString("email"));
        candidate.setRoleName(rs.getString("role_name"));
        candidate.setScore(rs.getDouble("score"));
        candidate.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return candidate;
    };

    public ShortlistedCandidateRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveIfAbsent(ScreeningReport report, Resume resume) {
        String sql = """
                INSERT IGNORE INTO shortlisted_candidates (
                    resume_id, report_id, candidate_name, email, role_name, score
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setLong(1, resume.getId());
                ps.setLong(2, report.getId());
                ps.setString(3, resume.getCandidateName());
                ps.setString(4, resume.getEmail());
                ps.setString(5, report.getRoleName());
                ps.setDouble(6, report.getScore());
                return ps;
            }, keyHolder);
        } catch (DataAccessException exception) {
            throw new DatabaseOperationException("Unable to save shortlisted candidate", exception);
        }
    }

    public List<ShortlistedCandidate> findAll() {
        return jdbcTemplate.query("SELECT * FROM shortlisted_candidates ORDER BY score DESC, created_at DESC", rowMapper);
    }

    public long count() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM shortlisted_candidates", Long.class);
        return count == null ? 0 : count;
    }
}
