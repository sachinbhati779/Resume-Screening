package com.resumescreening.exception;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.resumescreening.dto.ApiResponseDTO;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(IncompleteResumeException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleIncompleteResume(IncompleteResumeException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage(), "INCOMPLETE_RESUME");
    }

    @ExceptionHandler(InvalidJobRoleException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleInvalidJobRole(InvalidJobRoleException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage(), "INVALID_JOB_ROLE");
    }

    @ExceptionHandler(CandidateNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleCandidateNotFound(CandidateNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), "CANDIDATE_NOT_FOUND");
    }

    @ExceptionHandler(InterviewSessionNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleInterviewSessionNotFound(
            InterviewSessionNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), "INTERVIEW_SESSION_NOT_FOUND");
    }

    @ExceptionHandler(LiveInterviewNotFoundException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleLiveInterviewNotFound(
            LiveInterviewNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), "LIVE_INTERVIEW_NOT_FOUND");
    }

    @ExceptionHandler({DatabaseOperationException.class, DataAccessException.class})
    public ResponseEntity<ApiResponseDTO<Void>> handleDatabaseException(RuntimeException exception) {
        log.error("Database operation failed", exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Database operation failed", "DATABASE_OPERATION_FAILED");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return error(HttpStatus.BAD_REQUEST, message, "VALIDATION_ERROR");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleIllegalArgument(IllegalArgumentException exception) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage(), "BAD_REQUEST");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<Void>> handleUnexpected(Exception exception) {
        log.error("Unexpected backend error", exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected backend error", "INTERNAL_ERROR");
    }

    private ResponseEntity<ApiResponseDTO<Void>> error(HttpStatus status, String message, String errorCode) {
        return ResponseEntity.status(status).body(ApiResponseDTO.error(message, errorCode));
    }
}
