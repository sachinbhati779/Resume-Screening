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

import com.resumescreening.exception.DatabaseOperationException;
import com.resumescreening.model.LiveInterview;

@Repository
public class LiveInterviewRepository {

    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<LiveInterview> rowMapper = (rs, rowNum) -> {
        LiveInterview interview = new LiveInterview();
        interview.setId(rs.getLong("id"));
        interview.setCandidateId(rs.getLong("candidate_id"));
        interview.setRoleId(rs.getLong("role_id"));
        interview.setRoomName(rs.getString("room_name"));
        interview.setHostToken(rs.getString("host_token"));
        interview.setCandidateToken(rs.getString("candidate_token"));
        interview.setStatus(rs.getString("status"));
        interview.setRecordingPath(rs.getString("recording_path"));
        interview.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        interview.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return interview;
    };

    public LiveInterviewRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public LiveInterview save(LiveInterview interview) {
        String sql = """
                INSERT INTO live_interviews (
                    candidate_id, role_id, room_name, host_token, candidate_token, status, recording_path
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setLong(1, interview.getCandidateId());
                ps.setLong(2, interview.getRoleId());
                ps.setString(3, interview.getRoomName());
                ps.setString(4, interview.getHostToken());
                ps.setString(5, interview.getCandidateToken());
                ps.setString(6, interview.getStatus());
                ps.setString(7, interview.getRecordingPath());
                return ps;
            }, keyHolder);
            Number key = keyHolder.getKey();
            if (key == null) {
                throw new DatabaseOperationException("Failed to create live interview", null);
            }
            return findById(key.longValue()).orElseThrow(
                    () -> new DatabaseOperationException("Created live interview not found", null));
        } catch (DataAccessException exception) {
            throw new DatabaseOperationException("Unable to save live interview", exception);
        }
    }

    public Optional<LiveInterview> findById(Long id) {
        List<LiveInterview> interviews = jdbcTemplate.query(
                "SELECT * FROM live_interviews WHERE id = ?",
                rowMapper,
                id);
        return interviews.stream().findFirst();
    }

    public Optional<LiveInterview> findByHostToken(String token) {
        List<LiveInterview> interviews = jdbcTemplate.query(
                "SELECT * FROM live_interviews WHERE host_token = ?",
                rowMapper,
                token);
        return interviews.stream().findFirst();
    }

    public Optional<LiveInterview> findByCandidateToken(String token) {
        List<LiveInterview> interviews = jdbcTemplate.query(
                "SELECT * FROM live_interviews WHERE candidate_token = ?",
                rowMapper,
                token);
        return interviews.stream().findFirst();
    }
}
