package com.resumescreening.service;

import com.resumescreening.dto.ATSScoringResultDTO;
import com.resumescreening.dto.ScreeningRequestDTO;
import com.resumescreening.dto.ScreeningResponseDTO;
import com.resumescreening.exception.InvalidJobRoleException;
import com.resumescreening.model.JobRole;
import com.resumescreening.model.Resume;
import com.resumescreening.model.ScreeningReport;
import com.resumescreening.repository.JobRoleRepository;
import com.resumescreening.repository.ResumeRepository;
import com.resumescreening.repository.ScreeningReportRepository;
import com.resumescreening.repository.ShortlistedCandidateRepository;
import com.resumescreening.util.ScreeningConstants;
import com.resumescreening.util.ScreeningStatusResolver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ScreeningService {

    private static final Logger log = LoggerFactory.getLogger(ScreeningService.class);

    private final JobRoleRepository jobRoleRepository;
    private final ResumeRepository resumeRepository;
    private final ScreeningReportRepository screeningReportRepository;
    private final ShortlistedCandidateRepository shortlistedCandidateRepository;
    private final RuleBasedScoringService scoringService;
    private final ExecutorService screeningExecutor;

    public ScreeningService(
            JobRoleRepository jobRoleRepository,
            ResumeRepository resumeRepository,
            ScreeningReportRepository screeningReportRepository,
            ShortlistedCandidateRepository shortlistedCandidateRepository,
            RuleBasedScoringService scoringService,
            ExecutorService screeningExecutor) {
        this.jobRoleRepository = jobRoleRepository;
        this.resumeRepository = resumeRepository;
        this.screeningReportRepository = screeningReportRepository;
        this.shortlistedCandidateRepository = shortlistedCandidateRepository;
        this.scoringService = scoringService;
        this.screeningExecutor = screeningExecutor;
    }

    public List<ScreeningResponseDTO> screen(ScreeningRequestDTO request) {
        JobRole role = jobRoleRepository.findById(request.roleId())
                .orElseThrow(() -> new InvalidJobRoleException("Job role not found: " + request.roleId()));
        List<Resume> resumes = resumeRepository.findByIds(request.resumeIds());
        List<ScreeningResponseDTO> results = Collections.synchronizedList(new ArrayList<>());
        List<Callable<Void>> tasks = resumes.stream()
                .map(resume -> (Callable<Void>) () -> {
                    results.add(processResume(resume, role));
                    return null;
                })
                .toList();

        try {
            List<Future<Void>> futures = screeningExecutor.invokeAll(tasks);
            for (Future<Void> future : futures) {
                future.get();
            }
            log.info("Processed {} resumes for role {}", results.size(), role.getRoleName());
            return results.stream()
                    .sorted((left, right) -> Double.compare(right.score(), left.score()))
                    .toList();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Resume screening interrupted", exception);
        } catch (ExecutionException exception) {
            if (exception.getCause() instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new IllegalStateException("Resume screening failed", exception.getCause());
        } catch (Exception exception) {
            throw new IllegalStateException("Resume screening failed", exception);
        }
    }

    public List<ScreeningResponseDTO> findAllReports() {
        return screeningReportRepository.findAll().stream().map(this::toResponse).toList();
    }

    public ScreeningResponseDTO findReportById(Long id) {
        return screeningReportRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Screening report not found: " + id));
    }

    private ScreeningResponseDTO processResume(Resume resume, JobRole role) {
        ATSScoringResultDTO atsResult = scoringService.calculateAtsResult(resume, role);
        double score = atsResult.score();
        String status = atsResult.status();
        ScreeningReport report = new ScreeningReport();
        report.setResumeId(resume.getId());
        report.setRoleId(role.getId());
        report.setCandidateName(resume.getCandidateName());
        report.setRoleName(role.getRoleName());
        report.setScore(score);
        report.setStatus(status);
        report.setRemarks(atsResult.remarks());
        report.setMatchedKeywords(atsResult.matchedKeywords());
        report.setMissingKeywords(atsResult.missingKeywords());
        report.setSkillsScore(atsResult.scoreBreakdown().skillsScore());
        report.setExperienceScore(atsResult.scoreBreakdown().experienceScore());
        report.setProjectScore(atsResult.scoreBreakdown().projectScore());
        report.setEducationScore(atsResult.scoreBreakdown().educationScore());
        report.setKeywordScore(atsResult.scoreBreakdown().keywordScore());
        report.setAtsComplete(atsResult.atsChecks().complete());
        report.setAtsReadable(atsResult.atsChecks().readable());
        report.setAtsSimpleFormatting(atsResult.atsChecks().simpleFormatting());
        report.setAtsRequiredSections(atsResult.atsChecks().hasRequiredSections());
        report.setAtsIssues(atsResult.atsChecks().issues());
        report.setExplanation(atsResult.explanation());
        ScreeningReport savedReport = screeningReportRepository.save(report);
        if (ScreeningConstants.STATUS_SHORTLISTED.equals(status)) {
            shortlistedCandidateRepository.saveIfAbsent(savedReport, resume);
        }
        return toResponse(savedReport);
    }

    private ScreeningResponseDTO toResponse(ScreeningReport report) {
        return new ScreeningResponseDTO(
                report.getId(),
                report.getResumeId(),
                report.getRoleId(),
                report.getCandidateName(),
                report.getRoleName(),
                report.getScore(),
                report.getStatus(),
                report.getRemarks(),
                report.getMatchedKeywords(),
                report.getMissingKeywords(),
                new com.resumescreening.dto.ATSScoreBreakdownDTO(
                        report.getSkillsScore(),
                        report.getExperienceScore(),
                        report.getProjectScore(),
                        report.getEducationScore(),
                        report.getKeywordScore()),
                new com.resumescreening.dto.ATSChecksDTO(
                        report.isAtsComplete(),
                        report.isAtsReadable(),
                        report.isAtsSimpleFormatting(),
                        report.isAtsRequiredSections(),
                        report.getAtsIssues()),
                report.getExplanation(),
                report.getCreatedAt()
        );
    }
}
