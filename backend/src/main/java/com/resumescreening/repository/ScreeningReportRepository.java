package com.resumescreening.repository;

import com.resumescreening.dto.CandidateRankingDTO;
import com.resumescreening.dto.RecentActivityDTO;
import com.resumescreening.exception.DatabaseOperationException;
import com.resumescreening.model.ScreeningReport;
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
public class ScreeningReportRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<ScreeningReport> rowMapper = (rs, rowNum) -> {
        ScreeningReport report = new ScreeningReport();
        report.setId(rs.getLong("id"));
        report.setResumeId(rs.getLong("resume_id"));
        report.setRoleId(rs.getLong("role_id"));
        report.setCandidateName(rs.getString("candidate_name"));
        report.setRoleName(rs.getString("role_name"));
        report.setScore(rs.getDouble("score"));
        report.setStatus(rs.getString("status"));
        report.setRemarks(rs.getString("remarks"));
        report.setMatchedKeywords(StringListConverter.fromCsv(rs.getString("matched_keywords")));
        report.setMissingKeywords(StringListConverter.fromCsv(rs.getString("missing_keywords")));
        report.setSkillsScore(rs.getDouble("skills_score"));
        report.setExperienceScore(rs.getDouble("experience_score"));
        report.setProjectScore(rs.getDouble("project_score"));
        report.setEducationScore(rs.getDouble("education_score"));
        report.setKeywordScore(rs.getDouble("keyword_score"));
        report.setAtsComplete(rs.getBoolean("ats_complete"));
        report.setAtsReadable(rs.getBoolean("ats_readable"));
        report.setAtsSimpleFormatting(rs.getBoolean("ats_simple_formatting"));
        report.setAtsRequiredSections(rs.getBoolean("ats_required_sections"));
        report.setAtsIssues(StringListConverter.fromCsv(rs.getString("ats_issues")));
        report.setExplanation(rs.getString("explanation"));
        report.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return report;
    };

    public ScreeningReportRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ScreeningReport save(ScreeningReport report) {
        String sql = """
                INSERT INTO screening_reports (
                    resume_id, role_id, candidate_name, role_name, score, status, remarks,
                    matched_keywords, missing_keywords, skills_score, experience_score,
                    project_score, education_score, keyword_score, ats_complete, ats_readable,
                    ats_simple_formatting, ats_required_sections, ats_issues, explanation
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setLong(1, report.getResumeId());
                ps.setLong(2, report.getRoleId());
                ps.setString(3, report.getCandidateName());
                ps.setString(4, report.getRoleName());
                ps.setDouble(5, report.getScore());
                ps.setString(6, report.getStatus());
                ps.setString(7, report.getRemarks());
                ps.setString(8, StringListConverter.toCsv(report.getMatchedKeywords()));
                ps.setString(9, StringListConverter.toCsv(report.getMissingKeywords()));
                ps.setDouble(10, report.getSkillsScore());
                ps.setDouble(11, report.getExperienceScore());
                ps.setDouble(12, report.getProjectScore());
                ps.setDouble(13, report.getEducationScore());
                ps.setDouble(14, report.getKeywordScore());
                ps.setBoolean(15, report.isAtsComplete());
                ps.setBoolean(16, report.isAtsReadable());
                ps.setBoolean(17, report.isAtsSimpleFormatting());
                ps.setBoolean(18, report.isAtsRequiredSections());
                ps.setString(19, StringListConverter.toCsv(report.getAtsIssues()));
                ps.setString(20, report.getExplanation());
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key == null) {
                throw new DatabaseOperationException("Failed to create screening report", null);
            }
            return findById(key.longValue()).orElseThrow(
                    () -> new DatabaseOperationException("Created screening report not found", null));
        } catch (DataAccessException exception) {
            throw new DatabaseOperationException("Unable to save screening report", exception);
        }
    }

    public List<ScreeningReport> findAll() {
        return jdbcTemplate.query("SELECT * FROM screening_reports ORDER BY created_at DESC", rowMapper);
    }

    public Optional<ScreeningReport> findById(Long id) {
        List<ScreeningReport> reports = jdbcTemplate.query(
                "SELECT * FROM screening_reports WHERE id = ?", rowMapper, id);
        return reports.stream().findFirst();
    }

    public List<CandidateRankingDTO> findRanking() {
        String sql = """
                SELECT sr.id AS report_id, r.id AS resume_id, r.candidate_name, r.email, sr.role_name,
                       r.skills, r.experience_years, sr.score, sr.status, sr.remarks,
                       MAX(ir.final_score) AS interview_score,
                       COALESCE(MAX(hd.decision),
                           CASE
                               WHEN (sr.score * 0.6) + (COALESCE(MAX(ir.final_score), 0) * 0.4) >= 80 THEN 'HIRE'
                               WHEN (sr.score * 0.6) + (COALESCE(MAX(ir.final_score), 0) * 0.4) >= 60 THEN 'HOLD'
                               ELSE 'REJECT'
                           END
                       ) AS hiring_decision
                FROM screening_reports sr
                JOIN resumes r ON r.id = sr.resume_id
                LEFT JOIN interview_sessions ins ON ins.candidate_id = r.id
                LEFT JOIN interview_reports ir ON ir.session_id = ins.id
                LEFT JOIN hiring_decisions hd ON hd.report_id = sr.id
                GROUP BY sr.id, r.id, r.candidate_name, r.email, sr.role_name,
                         r.skills, r.experience_years, sr.score, sr.status, sr.remarks, sr.created_at
                ORDER BY ((sr.score * 0.6) + (COALESCE(MAX(ir.final_score), 0) * 0.4)) DESC, sr.score DESC, sr.created_at DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new CandidateRankingDTO(
                rs.getLong("report_id"),
                rs.getLong("resume_id"),
                rs.getString("candidate_name"),
                rs.getString("email"),
                rs.getString("role_name"),
                StringListConverter.fromCsv(rs.getString("skills")),
                rs.getDouble("experience_years"),
                rs.getDouble("score"),
                rs.getDouble("score"),
                interviewScore(rs.getObject("interview_score")),
                round((rs.getDouble("score") * 0.6) + (nullToZero(rs.getObject("interview_score")) * 0.4)),
                rs.getString("status"),
                rs.getString("remarks"),
                rs.getString("hiring_decision")
        ));
    }

    private Double interviewScore(Object value) {
        return value == null ? null : ((Number) value).doubleValue();
    }

    private double nullToZero(Object value) {
        return value == null ? 0 : ((Number) value).doubleValue();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public long countByStatus(String status) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM screening_reports WHERE status = ?", Long.class, status);
        return count == null ? 0 : count;
    }

    public double averageScore() {
        Double average = jdbcTemplate.queryForObject("SELECT COALESCE(AVG(score), 0) FROM screening_reports", Double.class);
        return average == null ? 0 : average;
    }

    public List<RecentActivityDTO> findRecentActivity(int limit) {
        String sql = """
                SELECT 'SCREENING' AS type, candidate_name AS title,
                       CONCAT(role_name, ' - ', status, ' (', ROUND(score, 1), ')') AS description,
                       created_at
                FROM screening_reports
                ORDER BY created_at DESC
                LIMIT ?
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> new RecentActivityDTO(
                rs.getString("type"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getTimestamp("created_at").toLocalDateTime()
        ), limit);
    }

    public boolean existsShortlistedByResumeId(Long resumeId) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM screening_reports WHERE resume_id = ? AND status = 'SHORTLISTED'",
                Long.class,
                resumeId);
        return count != null && count > 0;
    }
}
