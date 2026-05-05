package com.resumescreening.service;

import com.resumescreening.dto.HiringDecisionDTO;
import com.resumescreening.dto.HiringDecisionRequestDTO;
import com.resumescreening.model.HiringDecision;
import com.resumescreening.model.ScreeningReport;
import com.resumescreening.repository.HiringDecisionRepository;
import com.resumescreening.repository.ScreeningReportRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class HiringService {

    private final HiringDecisionRepository hiringDecisionRepository;
    private final ScreeningReportRepository screeningReportRepository;

    public HiringService(
            HiringDecisionRepository hiringDecisionRepository,
            ScreeningReportRepository screeningReportRepository) {
        this.hiringDecisionRepository = hiringDecisionRepository;
        this.screeningReportRepository = screeningReportRepository;
    }

    public HiringDecisionDTO create(HiringDecisionRequestDTO request) {
        ScreeningReport report = screeningReportRepository.findById(request.reportId())
                .orElseThrow(() -> new IllegalArgumentException("Screening report not found: " + request.reportId()));
        String decisionValue = normalizeDecision(request.decision());

        HiringDecision decision = new HiringDecision();
        decision.setCandidateId(request.candidateId());
        decision.setResumeId(request.resumeId());
        decision.setReportId(request.reportId());
        decision.setCandidateName(report.getCandidateName());
        decision.setRoleName(report.getRoleName());
        decision.setDecision(decisionValue);
        decision.setNotes(request.notes());

        return toDto(hiringDecisionRepository.save(decision));
    }

    public List<HiringDecisionDTO> findAll() {
        return hiringDecisionRepository.findAll().stream().map(this::toDto).toList();
    }

    private String normalizeDecision(String decision) {
        String normalized = decision == null ? "" : decision.trim().toUpperCase(Locale.ROOT);
        if (!List.of("HIRE", "HOLD", "REJECT").contains(normalized)) {
            throw new IllegalArgumentException("Hiring decision must be HIRE, HOLD, or REJECT");
        }
        return normalized;
    }

    private HiringDecisionDTO toDto(HiringDecision decision) {
        return new HiringDecisionDTO(
                decision.getId(),
                decision.getCandidateId(),
                decision.getResumeId(),
                decision.getReportId(),
                decision.getCandidateName(),
                decision.getRoleName(),
                decision.getDecision(),
                decision.getNotes(),
                decision.getCreatedAt());
    }
}
