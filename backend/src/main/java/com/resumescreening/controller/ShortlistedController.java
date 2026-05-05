package com.resumescreening.controller;

import com.resumescreening.dto.ApiResponseDTO;
import com.resumescreening.dto.ShortlistedCandidateDTO;
import com.resumescreening.service.ShortlistedCandidateService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shortlisted")
public class ShortlistedController {

    private final ShortlistedCandidateService shortlistedCandidateService;

    public ShortlistedController(ShortlistedCandidateService shortlistedCandidateService) {
        this.shortlistedCandidateService = shortlistedCandidateService;
    }

    @GetMapping
    public ApiResponseDTO<List<ShortlistedCandidateDTO>> findAll() {
        return ApiResponseDTO.success("Shortlisted candidates loaded", shortlistedCandidateService.findAll());
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export() {
        byte[] bytes = shortlistedCandidateService.exportCsv().getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("shortlisted-candidates.csv").build().toString())
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(bytes);
    }
}
