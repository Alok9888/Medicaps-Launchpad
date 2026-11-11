package com.project.backend.common.exception;

import java.util.List;

public class ValidationException extends RuntimeException {
    private final String code;
    private final List<String> details;

    public ValidationException(String message, List<String> details) {
        this("VALIDATION_FAILED", message, details);
    }

    public ValidationException(String code, String message, List<String> details) {
        super(message);
        this.code = code;
        this.details = details;
    }

    public String getCode() {
        return code;
    }

    public List<String> getDetails() {
        return details;
    }
}