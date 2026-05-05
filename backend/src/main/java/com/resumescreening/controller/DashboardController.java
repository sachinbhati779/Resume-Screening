package com.resumescreening.controller;

import com.resumescreening.dto.ApiResponseDTO;
import com.resumescreening.dto.DashboardSummaryDTO;
import com.resumescreening.dto.RecentActivityDTO;
import com.resumescreening.service.DatabaseService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DatabaseService databaseService;

    public DashboardController(DatabaseService databaseService) {
        this.databaseService = databaseService;
    }

    @GetMapping("/summary")
    public ApiResponseDTO<DashboardSummaryDTO> summary() {
        return ApiResponseDTO.success("Dashboard summary loaded", databaseService.dashboardSummary());
    }

    @GetMapping("/recent-activity")
    public ApiResponseDTO<List<RecentActivityDTO>> recentActivity() {
        return ApiResponseDTO.success("Recent activity loaded", databaseService.recentActivity());
    }
}
