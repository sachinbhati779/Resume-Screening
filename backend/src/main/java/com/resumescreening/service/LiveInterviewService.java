package com.resumescreening.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.resumescreening.dto.LiveInterviewAccessDTO;
import com.resumescreening.dto.LiveInterviewCreateRequestDTO;
import com.resumescreening.dto.LiveInterviewResponseDTO;
import com.resumescreening.exception.CandidateNotFoundException;
import com.resumescreening.exception.InvalidJobRoleException;
import com.resumescreening.exception.LiveInterviewNotFoundException;
import com.resumescreening.model.JobRole;
import com.resumescreening.model.LiveInterview;
import com.resumescreening.model.Resume;
import com.resumescreening.repository.JobRoleRepository;
import com.resumescreening.repository.LiveInterviewRepository;
import com.resumescreening.repository.ResumeRepository;
import com.resumescreening.repository.ScreeningReportRepository;

@Service
public class LiveInterviewService {

    private static final String STATUS_SCHEDULED = "SCHEDULED";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final LiveInterviewRepository liveInterviewRepository;
    private final ResumeRepository resumeRepository;
    private final JobRoleRepository jobRoleRepository;
    private final ScreeningReportRepository screeningReportRepository;
    private final String frontendBaseUrl;
    private final String jitsiBaseUrl;
    private final String roomPrefix;

    public LiveInterviewService(
            LiveInterviewRepository liveInterviewRepository,
            ResumeRepository resumeRepository,
            JobRoleRepository jobRoleRepository,
            ScreeningReportRepository screeningReportRepository,
            @Value("${app.frontend.base-url:http://localhost:3000}") String frontendBaseUrl,
            @Value("${app.jitsi.base-url:http://localhost:8000}") String jitsiBaseUrl,
            @Value("${app.jitsi.room-prefix:rs}") String roomPrefix) {
        this.liveInterviewRepository = liveInterviewRepository;
        this.resumeRepository = resumeRepository;
        this.jobRoleRepository = jobRoleRepository;
        this.screeningReportRepository = screeningReportRepository;
        this.frontendBaseUrl = trimTrailingSlash(frontendBaseUrl);
        this.jitsiBaseUrl = trimTrailingSlash(jitsiBaseUrl);
        this.roomPrefix = roomPrefix == null || roomPrefix.isBlank() ? "rs" : roomPrefix.trim();
    }

    public LiveInterviewResponseDTO create(LiveInterviewCreateRequestDTO request) {
        Resume resume = resumeRepository.findById(request.candidateId())
                .orElseThrow(() -> new CandidateNotFoundException("Candidate not found: " + request.candidateId()));
        JobRole role = jobRoleRepository.findById(request.roleId())
                .orElseThrow(() -> new InvalidJobRoleException("Job role not found: " + request.roleId()));
        if (!screeningReportRepository.existsShortlistedByResumeId(resume.getId())) {
            throw new CandidateNotFoundException("Candidate must be shortlisted before live interview");
        }

        LiveInterview interview = new LiveInterview();
        interview.setCandidateId(resume.getId());
        interview.setRoleId(role.getId());
        interview.setRoomName(buildRoomName(role.getRoleName()));
        interview.setHostToken(generateToken());
        interview.setCandidateToken(generateToken());
        interview.setStatus(STATUS_SCHEDULED);
        interview.setRecordingPath(null);
        interview.setCreatedAt(LocalDateTime.now());
        LiveInterview saved = liveInterviewRepository.save(interview);

        String hostLink = frontendBaseUrl + "/interview/live/" + saved.getHostToken();
        String candidateLink = frontendBaseUrl + "/interview/live/" + saved.getCandidateToken();

        return new LiveInterviewResponseDTO(
                saved.getId(),
                saved.getCandidateId(),
                saved.getRoleId(),
                saved.getRoomName(),
                saved.getHostToken(),
                saved.getCandidateToken(),
                hostLink,
                candidateLink,
                saved.getStatus(),
                saved.getCreatedAt()
        );
    }

    public LiveInterviewAccessDTO access(String token) {
        LiveInterview interview = liveInterviewRepository.findByHostToken(token)
                .orElseGet(() -> liveInterviewRepository.findByCandidateToken(token).orElse(null));
        if (interview == null) {
            throw new LiveInterviewNotFoundException("Live interview not found");
        }
        boolean isHost = token.equals(interview.getHostToken());
        Resume resume = resumeRepository.findById(interview.getCandidateId())
                .orElseThrow(() -> new CandidateNotFoundException("Candidate not found: " + interview.getCandidateId()));
        JobRole role = jobRoleRepository.findById(interview.getRoleId())
                .orElseThrow(() -> new InvalidJobRoleException("Job role not found: " + interview.getRoleId()));

        return new LiveInterviewAccessDTO(
                interview.getId(),
                interview.getRoomName(),
                jitsiBaseUrl + "/" + interview.getRoomName(),
                resume.getCandidateName(),
                role.getRoleName(),
                isHost,
                interview.getStatus()
        );
    }

    private String generateToken() {
        byte[] bytes = new byte[24];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String buildRoomName(String roleName) {
        String normalized = roleName == null ? "role" : roleName.toLowerCase().replaceAll("[^a-z0-9]+", "-");
        String suffix = generateToken().substring(0, 8).toLowerCase();
        return roomPrefix + "-" + normalized + "-" + suffix;
    }

    private String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
