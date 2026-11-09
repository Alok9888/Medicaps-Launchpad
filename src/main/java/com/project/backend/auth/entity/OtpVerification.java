package com.project.backend.auth.entity;

import lombok.*;

import java.time.Instant;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OtpVerification {

    private String otpHash;                // BCrypt hash of the plain OTP
    private Instant expiresAt;             // TTL - rather than using spring scheduler this one is bttr
    private int attempts;                  // verification attempts used
    private Instant lastSent;              // for resend‑cool‑down
}
