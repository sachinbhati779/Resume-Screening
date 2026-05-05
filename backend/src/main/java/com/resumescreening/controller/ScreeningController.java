package com.resumescreening.controller;

import com.resumescreening.dto.ApiResponseDTO;
import com.resumescreening.dto.CandidateRankingDTO;
import com.resumescreening.dto.ScreeningRequestDTO;
import com.resumescreening.dto.ScreeningResponseDTO;
import com.resumescreening.service.RankingService;
import com.resumescreening.service.ScreeningService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ScreeningController {

    private final ScreeningService screeningService;
    private final RankingService rankingService;

    public ScreeningController(ScreeningService screeningService, RankingService rankingService) {
        this.screeningService = screeningService;
        this.rankingService = rankingService;
    }

    @PostMapping("/api/screen")
    public ApiResponseDTO<List<ScreeningResponseDTO>> screen(@Valid @RequestBody ScreeningRequestDTO request) {
        return ApiResponseDTO.success("Screening completed", screeningService.screen(request));
    }

    @GetMapping("/api/screening-reports")
    public ApiResponseDTO<List<ScreeningResponseDTO>> reports() {
        return ApiResponseDTO.success("Screening reports loaded", screeningService.findAllReports());
    }

    @GetMapping("/api/screening-reports/{id}")
    public ApiResponseDTO<ScreeningResponseDTO> reportById(@PathVariable Long id) {
        return ApiResponseDTO.success("Screening report loaded", screeningService.findReportById(id));
    }

    @GetMapping("/api/candidates/ranking")
    public ApiResponseDTO<List<CandidateRankingDTO>> ranking() {
        return ApiResponseDTO.success("Candidate ranking loaded", rankingService.findRanking());
    }
}
