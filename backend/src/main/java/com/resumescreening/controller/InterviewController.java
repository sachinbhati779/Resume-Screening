package com.resumescreening.controller;

import com.resumescreening.dto.ApiResponseDTO;
import com.resumescreening.dto.InterviewAnswerDTO;
import com.resumescreening.dto.InterviewQuestionDTO;
import com.resumescreening.dto.InterviewResultDTO;
import com.resumescreening.dto.InterviewStartRequestDTO;
import com.resumescreening.dto.InterviewStartResponseDTO;
import com.resumescreening.service.InterviewService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/interview")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseDTO<InterviewStartResponseDTO> start(@Valid @RequestBody InterviewStartRequestDTO request) {
        return ApiResponseDTO.success("Interview started", interviewService.start(request));
    }

    @GetMapping("/{sessionId}/question")
    public ApiResponseDTO<InterviewQuestionDTO> question(@PathVariable Long sessionId) {
        return ApiResponseDTO.success("Interview question loaded", interviewService.getCurrentQuestion(sessionId));
    }

    @PostMapping("/{sessionId}/answer")
    public ApiResponseDTO<InterviewAnswerDTO> answer(
            @PathVariable Long sessionId,
            @Valid @RequestBody InterviewAnswerDTO request) {
        return ApiResponseDTO.success("Interview answer evaluated", interviewService.submitAnswer(sessionId, request));
    }

    @GetMapping("/{sessionId}/result")
    public ApiResponseDTO<InterviewResultDTO> result(@PathVariable Long sessionId) {
        return ApiResponseDTO.success("Interview result loaded", interviewService.getResult(sessionId));
    }
}
