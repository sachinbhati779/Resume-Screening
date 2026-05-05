package com.resumescreening.service;

import com.resumescreening.dto.CandidateRankingDTO;
import com.resumescreening.repository.ScreeningReportRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RankingService {

    private final ScreeningReportRepository screeningReportRepository;

    public RankingService(ScreeningReportRepository screeningReportRepository) {
        this.screeningReportRepository = screeningReportRepository;
    }

    public List<CandidateRankingDTO> findRanking() {
        return screeningReportRepository.findRanking();
    }
}
