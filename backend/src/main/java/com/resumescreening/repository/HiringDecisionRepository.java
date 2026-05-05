package com.resumescreening.repository;

import com.resumescreening.exception.DatabaseOperationException;
import com.resumescreening.model.HiringDecision;
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
public class HiringDecisionRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<HiringDecision> rowMapper = (rs, rowNum) -> {
        HiringDecision decision = new HiringDecision();
        decision.setId(rs.getLong("id"));
        decision.setCandidateId(rs.getObject("candidate_id") == null ? null : rs.getLong("candidate_id"));
        decision.setResumeId(rs.getLong("resume_id"));
        decision.setReportId(rs.getLong("report_id"));
        decision.setCandidateName(rs.getString("candidate_name"));
        decision.setRoleName(rs.getString("role_name"));
        decision.setDecision(rs.getString("decision"));
        decision.setNotes(rs.getString("notes"));
        decision.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return decision;
    };

    public HiringDecisionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public HiringDecision save(HiringDecision decision) {
        Optional<HiringDecision> existing = findByReportId(decision.getReportId());
        if (existing.isPresent()) {
            return update(existing.get().getId(), decision);
        }
        String sql = """
                INSERT INTO hiring_decisions (
                    candidate_id, resume_id, report_id, candidate_name, role_name, decision, notes
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                if (decision.getCandidateId() == null) {
                    ps.setObject(1, null);
                } else {
                    ps.setLong(1, decision.getCandidateId());
                }
                ps.setLong(2, decision.getResumeId());
                ps.setLong(3, decision.getReportId());
                ps.setString(4, decision.getCandidateName());
                ps.setString(5, decision.getRoleName());
                ps.setString(6, decision.getDecision());
                ps.setString(7, decision.getNotes());
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key == null) {
                throw new DatabaseOperationException("Failed to create hiring decision", null);
            }
            return findById(key.longValue()).orElseThrow(
                    () -> new DatabaseOperationException("Created hiring decision not found", null));
        } catch (DataAccessException exception) {
            throw new DatabaseOperationException("Unable to save hiring decision", exception);
        }
    }

    public List<HiringDecision> findAll() {
        return jdbcTemplate.query("SELECT * FROM hiring_decisions ORDER BY created_at DESC", rowMapper);
    }

    public Optional<HiringDecision> findById(Long id) {
        return jdbcTemplate.query("SELECT * FROM hiring_decisions WHERE id = ?", rowMapper, id)
                .stream()
                .findFirst();
    }

    public Optional<HiringDecision> findByReportId(Long reportId) {
        return jdbcTemplate.query("SELECT * FROM hiring_decisions WHERE report_id = ?", rowMapper, reportId)
                .stream()
                .findFirst();
    }

    private HiringDecision update(Long id, HiringDecision decision) {
        try {
            jdbcTemplate.update("""
                    UPDATE hiring_decisions
                    SET candidate_id = ?, resume_id = ?, candidate_name = ?, role_name = ?, decision = ?, notes = ?
                    WHERE id = ?
                    """,
                    decision.getCandidateId(),
                    decision.getResumeId(),
                    decision.getCandidateName(),
                    decision.getRoleName(),
                    decision.getDecision(),
                    decision.getNotes(),
                    id);
            return findById(id).orElseThrow(
                    () -> new DatabaseOperationException("Updated hiring decision not found", null));
        } catch (DataAccessException exception) {
            throw new DatabaseOperationException("Unable to update hiring decision", exception);
        }
    }
}
