package com.project.backend.auth.exception;

public class OtpCooldownException extends OtpException {
    public OtpCooldownException(String message) {
        super(message);
    }
}
