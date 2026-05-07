package com.resumescreening.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.resumescreening.dto.ApiResponseDTO;
import com.resumescreening.dto.LiveInterviewAccessDTO;
import com.resumescreening.dto.LiveInterviewCreateRequestDTO;
import com.resumescreening.dto.LiveInterviewResponseDTO;
import com.resumescreening.service.LiveInterviewService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/live-interviews")
public class LiveInterviewController {

    private final LiveInterviewService liveInterviewService;

    public LiveInterviewController(LiveInterviewService liveInterviewService) {
        this.liveInterviewService = liveInterviewService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseDTO<LiveInterviewResponseDTO> create(@Valid @RequestBody LiveInterviewCreateRequestDTO request) {
        return ApiResponseDTO.success("Live interview created", liveInterviewService.create(request));
    }

    @GetMapping("/{token}")
    public ApiResponseDTO<LiveInterviewAccessDTO> access(@PathVariable String token) {
        return ApiResponseDTO.success("Live interview loaded", liveInterviewService.access(token));
    }
}
