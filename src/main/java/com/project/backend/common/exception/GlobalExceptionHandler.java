package com.project.backend.common.exception;

import com.project.backend.common.api.ApiError;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(NotFoundException ex) {
        String code = mapNotFoundCode(ex.getCode());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiError.of(code, ex.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException ex) {
        String code = mapConflictCode(ex.getCode());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(code, ex.getMessage()));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiError> handleValidation(ValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiError.of(ex.getCode(), ex.getMessage(), ex.getDetails()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiError.of("VALIDATION_FAILED", "Validation failed", details));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> details = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiError.of("VALIDATION_FAILED", "Validation failed", details));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiError.of("VALIDATION_FAILED", "Invalid parameter: " + ex.getName()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();
        String code = "CONFLICT";
        if (message != null && message.toLowerCase().contains("duplicate")) {
            if (message.contains("slug")) code = "DUPLICATE_SLUG";
            else if (message.contains("name")) code = "DUPLICATE_NAME";
            else code = "DUPLICATE_VALUE";
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiError.of(code, message != null ? message : "Data integrity violation"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleInternal(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiError.of("INTERNAL_ERROR", "An unexpected error occurred"));
    }

    private String mapNotFoundCode(String code) {
        return switch (code) {
            case "QUESTION_NOT_FOUND" -> "QUESTION_NOT_FOUND";
            case "TEST_CASE_NOT_FOUND" -> "TEST_CASE_NOT_FOUND";
            case "LANGUAGE_NOT_FOUND" -> "LANGUAGE_NOT_FOUND";
            default -> "NOT_FOUND";
        };
    }

    private String mapConflictCode(String code) {
        return switch (code) {
            case "DUPLICATE_SLUG" -> "DUPLICATE_SLUG";
            case "DUPLICATE_NAME" -> "DUPLICATE_NAME";
            case "STALE_VERSION" -> "STALE_VERSION";
            case "ILLEGAL_STATUS_TRANSITION" -> "ILLEGAL_STATUS_TRANSITION";
            case "CATALOG_IN_USE" -> "CATALOG_IN_USE";
            default -> "CONFLICT";
        };
    }
}