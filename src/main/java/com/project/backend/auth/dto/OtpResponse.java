package com.project.backend.auth.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OtpResponse {

    private String message;          // e.g. "OTP sent"
    private long expiresIn;          // seconds until expiration (optional)
}
