package com.resumescreening.service;

import com.resumescreening.dto.DashboardSummaryDTO;
import com.resumescreening.dto.RecentActivityDTO;
import com.resumescreening.repository.ResumeRepository;
import com.resumescreening.repository.ScreeningReportRepository;
import com.resumescreening.repository.ShortlistedCandidateRepository;
import com.resumescreening.util.ScreeningConstants;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DatabaseService {

    private final ResumeRepository resumeRepository;
    private final ScreeningReportRepository screeningReportRepository;
    private final ShortlistedCandidateRepository shortlistedCandidateRepository;

    public DatabaseService(
            ResumeRepository resumeRepository,
            ScreeningReportRepository screeningReportRepository,
            ShortlistedCandidateRepository shortlistedCandidateRepository) {
        this.resumeRepository = resumeRepository;
        this.screeningReportRepository = screeningReportRepository;
        this.shortlistedCandidateRepository = shortlistedCandidateRepository;
    }

    public DashboardSummaryDTO dashboardSummary() {
        return new DashboardSummaryDTO(
                resumeRepository.count(),
                shortlistedCandidateRepository.count(),
                screeningReportRepository.countByStatus(ScreeningConstants.STATUS_REJECTED),
                round(screeningReportRepository.averageScore())
        );
    }

    public List<RecentActivityDTO> recentActivity() {
        return screeningReportRepository.findRecentActivity(10);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
