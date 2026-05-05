package com.resumescreening.repository;

import com.resumescreening.exception.DatabaseOperationException;
import com.resumescreening.model.InterviewAnswer;
import com.resumescreening.model.InterviewQuestion;
import com.resumescreening.model.InterviewReport;
import com.resumescreening.model.InterviewSession;
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
public class InterviewRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<InterviewSession> sessionRowMapper = (rs, rowNum) -> {
        InterviewSession session = new InterviewSession();
        session.setId(rs.getLong("id"));
        session.setCandidateId(rs.getLong("candidate_id"));
        session.setRoleName(rs.getString("role_name"));
        session.setCurrentQuestionIndex(rs.getInt("current_question_index"));
        session.setTotalScore(rs.getDouble("total_score"));
        session.setStatus(rs.getString("status"));
        session.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return session;
    };

    private final RowMapper<InterviewQuestion> questionRowMapper = (rs, rowNum) -> {
        InterviewQuestion question = new InterviewQuestion();
        question.setId(rs.getLong("id"));
        question.setSessionId(rs.getLong("session_id"));
        question.setQuestionText(rs.getString("question_text"));
        question.setExpectedKeywords(StringListConverter.fromCsv(rs.getString("expected_keywords")));
        question.setMarks(rs.getInt("marks"));
        return question;
    };

    private final RowMapper<InterviewReport> reportRowMapper = (rs, rowNum) -> {
        InterviewReport report = new InterviewReport();
        report.setId(rs.getLong("id"));
        report.setSessionId(rs.getLong("session_id"));
        report.setFinalScore(rs.getDouble("final_score"));
        report.setRecommendation(rs.getString("recommendation"));
        report.setStrengths(rs.getString("strengths"));
        report.setWeaknesses(rs.getString("weaknesses"));
        report.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return report;
    };

    public InterviewRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public InterviewSession saveSession(InterviewSession session) {
        String sql = """
                INSERT INTO interview_sessions (
                    candidate_id, role_name, current_question_index, total_score, status
                )
                VALUES (?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setLong(1, session.getCandidateId());
                ps.setString(2, session.getRoleName());
                ps.setInt(3, session.getCurrentQuestionIndex());
                ps.setDouble(4, session.getTotalScore());
                ps.setString(5, session.getStatus());
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key == null) {
                throw new DatabaseOperationException("Failed to create interview session", null);
            }
            return findSessionById(key.longValue()).orElseThrow(
                    () -> new DatabaseOperationException("Created interview session not found", null));
        } catch (DataAccessException exception) {
            throw new DatabaseOperationException("Unable to save interview session", exception);
        }
    }

    public Optional<InterviewSession> findSessionById(Long id) {
        List<InterviewSession> sessions = jdbcTemplate.query(
                "SELECT * FROM interview_sessions WHERE id = ?", sessionRowMapper, id);
        return sessions.stream().findFirst();
    }

    public void updateSession(Long sessionId, int currentQuestionIndex, double totalScore, String status) {
        jdbcTemplate.update("""
                UPDATE interview_sessions
                SET current_question_index = ?, total_score = ?, status = ?
                WHERE id = ?
                """, currentQuestionIndex, totalScore, status, sessionId);
    }

    public InterviewQuestion saveQuestion(InterviewQuestion question) {
        String sql = """
                INSERT INTO interview_questions (session_id, question_text, expected_keywords, marks)
                VALUES (?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setLong(1, question.getSessionId());
                ps.setString(2, question.getQuestionText());
                ps.setString(3, StringListConverter.toCsv(question.getExpectedKeywords()));
                ps.setInt(4, question.getMarks());
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key == null) {
                throw new DatabaseOperationException("Failed to create interview question", null);
            }
            return findQuestionById(key.longValue()).orElseThrow(
                    () -> new DatabaseOperationException("Created interview question not found", null));
        } catch (DataAccessException exception) {
            throw new DatabaseOperationException("Unable to save interview question", exception);
        }
    }

    public Optional<InterviewQuestion> findQuestionById(Long id) {
        List<InterviewQuestion> questions = jdbcTemplate.query(
                "SELECT * FROM interview_questions WHERE id = ?", questionRowMapper, id);
        return questions.stream().findFirst();
    }

    public List<InterviewQuestion> findQuestionsBySessionId(Long sessionId) {
        return jdbcTemplate.query(
                "SELECT * FROM interview_questions WHERE session_id = ? ORDER BY id ASC",
                questionRowMapper,
                sessionId);
    }

    public InterviewAnswer saveAnswer(InterviewAnswer answer) {
        String sql = """
                INSERT INTO interview_answers (session_id, question_id, answer_text, score, feedback)
                VALUES (?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setLong(1, answer.getSessionId());
                ps.setLong(2, answer.getQuestionId());
                ps.setString(3, answer.getAnswerText());
                ps.setDouble(4, answer.getScore());
                ps.setString(5, answer.getFeedback());
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            answer.setId(key == null ? null : key.longValue());
            return answer;
        } catch (DataAccessException exception) {
            throw new DatabaseOperationException("Unable to save interview answer", exception);
        }
    }

    public List<InterviewAnswer> findAnswersBySessionId(Long sessionId) {
        String sql = """
                SELECT id, session_id, question_id, answer_text, score, feedback
                FROM interview_answers
                WHERE session_id = ?
                ORDER BY id ASC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            InterviewAnswer answer = new InterviewAnswer();
            answer.setId(rs.getLong("id"));
            answer.setSessionId(rs.getLong("session_id"));
            answer.setQuestionId(rs.getLong("question_id"));
            answer.setAnswerText(rs.getString("answer_text"));
            answer.setScore(rs.getDouble("score"));
            answer.setFeedback(rs.getString("feedback"));
            return answer;
        }, sessionId);
    }

    public InterviewReport saveReport(InterviewReport report) {
        String sql = """
                INSERT INTO interview_reports (session_id, final_score, recommendation, strengths, weaknesses)
                VALUES (?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setLong(1, report.getSessionId());
                ps.setDouble(2, report.getFinalScore());
                ps.setString(3, report.getRecommendation());
                ps.setString(4, report.getStrengths());
                ps.setString(5, report.getWeaknesses());
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key == null) {
                throw new DatabaseOperationException("Failed to create interview report", null);
            }
            return findReportBySessionId(report.getSessionId()).orElseThrow(
                    () -> new DatabaseOperationException("Created interview report not found", null));
        } catch (DataAccessException exception) {
            throw new DatabaseOperationException("Unable to save interview report", exception);
        }
    }

    public Optional<InterviewReport> findReportBySessionId(Long sessionId) {
        List<InterviewReport> reports = jdbcTemplate.query(
                "SELECT * FROM interview_reports WHERE session_id = ?", reportRowMapper, sessionId);
        return reports.stream().findFirst();
    }
}
