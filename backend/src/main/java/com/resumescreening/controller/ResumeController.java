package com.resumescreening.controller;

import com.resumescreening.dto.ApiResponseDTO;
import com.resumescreening.dto.ResumeFileDTO;
import com.resumescreening.dto.ResumeRequestDTO;
import com.resumescreening.dto.ResumeResponseDTO;
import com.resumescreening.dto.ResumeUploadResponseDTO;
import com.resumescreening.service.ResumeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseDTO<ResumeResponseDTO> create(@Valid @RequestBody ResumeRequestDTO request) {
        return ApiResponseDTO.success("Resume submitted successfully", resumeService.create(request));
    }

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseDTO<ResumeUploadResponseDTO> upload(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "appliedRole", required = false) String appliedRole) {
        return ApiResponseDTO.success("Resume files processed", resumeService.upload(files, appliedRole));
    }

    @GetMapping
    public ApiResponseDTO<List<ResumeResponseDTO>> findAll() {
        return ApiResponseDTO.success("Resumes loaded", resumeService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponseDTO<ResumeResponseDTO> findById(@PathVariable Long id) {
        return ApiResponseDTO.success("Resume loaded", resumeService.findById(id));
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        ResumeFileDTO file = resumeService.findFileById(id);
        String fileName = file.fileName() == null || file.fileName().isBlank()
                ? "resume-" + id
                : file.fileName();
        String fileType = file.fileType() == null || file.fileType().isBlank()
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                : file.fileType();
        ResponseEntity.BodyBuilder response = ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType(fileType));
        if (file.fileSize() != null) {
            response.contentLength(file.fileSize());
        }
        return response.body(file.data());
    }
}
