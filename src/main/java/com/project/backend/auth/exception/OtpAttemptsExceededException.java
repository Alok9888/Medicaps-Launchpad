package com.project.backend.auth.exception;

public class OtpAttemptsExceededException extends OtpException {
    public OtpAttemptsExceededException(String message) {
        super(message);
    }
}
