package com.resumescreening.controller;

import com.resumescreening.dto.ApiResponseDTO;
import com.resumescreening.dto.HiringDecisionDTO;
import com.resumescreening.dto.HiringDecisionRequestDTO;
import com.resumescreening.service.HiringService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HiringController {

    private final HiringService hiringService;

    public HiringController(HiringService hiringService) {
        this.hiringService = hiringService;
    }

    @GetMapping("/api/hiring-decisions")
    public ApiResponseDTO<List<HiringDecisionDTO>> decisions() {
        return ApiResponseDTO.success("Hiring decisions loaded", hiringService.findAll());
    }

    @PostMapping("/api/hiring-decisions")
    public ApiResponseDTO<HiringDecisionDTO> decide(@Valid @RequestBody HiringDecisionRequestDTO request) {
        return ApiResponseDTO.success("Hiring decision saved", hiringService.create(request));
    }
}
