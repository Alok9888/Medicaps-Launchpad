package com.project.backend.auth.exception;

public class OtpExpiredException extends OtpException {
    public OtpExpiredException(String message) {
        super(message);
    }
}
