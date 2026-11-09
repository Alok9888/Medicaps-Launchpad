
package com.project.backend.auth.service;

import com.project.backend.auth.entity.OtpVerification;
import com.project.backend.auth.exception.*;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final RedisTemplate<String, OtpVerification> otpRedisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final EmailService emailService;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Value("${otp.ttl-seconds}")
    private long otpTtlSeconds;

    @Value("${otp.max-attempts}")
    private int maxAttempts;

    @Value("${otp.resend-cooldown-seconds}")
    private long resendCooldownSeconds;

    private static final String OTP_KEY_PREFIX = "otp:";
    private static final String OTP_LOCK_PREFIX = "otp-lock:";

    public void generateAndSendOtp(String email, String purpose) {

        String lockKey = OTP_LOCK_PREFIX + email;

        Boolean exists = stringRedisTemplate.hasKey(lockKey);

        if (Boolean.TRUE.equals(exists)) {
            throw new OtpCooldownException(
                    "Please wait before requesting a new OTP."
            );
        }

        String otp = String.format(
                "%06d",
                SECURE_RANDOM.nextInt(1_000_000)
        );

        String otpHash = passwordEncoder.encode(otp);

        Instant now = Instant.now();

        OtpVerification payload = OtpVerification.builder()
                .otpHash(otpHash)
                .expiresAt(now.plusSeconds(otpTtlSeconds))
                .attempts(0)
                .lastSent(now)
                .build();

        String otpKey = OTP_KEY_PREFIX + email;

        otpRedisTemplate.opsForValue().set(
                otpKey,
                payload,
                otpTtlSeconds,
                TimeUnit.SECONDS
        );

        stringRedisTemplate.opsForValue().set(
                lockKey,
                "locked",
                resendCooldownSeconds,
                TimeUnit.SECONDS
        );

        emailService.sendOtp(email, otp, purpose);
    }

    public void verifyOtp(String email, String rawOtp) {

        String otpKey = OTP_KEY_PREFIX + email;

        OtpVerification stored =
                otpRedisTemplate.opsForValue().get(otpKey);

        if (stored == null) {
            throw new OtpNotFoundException(
                    "OTP not found or has expired."
            );
        }

        if (Instant.now().isAfter(stored.getExpiresAt())) {
            otpRedisTemplate.delete(otpKey);

            throw new OtpExpiredException("OTP expired.");
        }

        if (stored.getAttempts() >= maxAttempts) {
            otpRedisTemplate.delete(otpKey);

            throw new OtpAttemptsExceededException(
                    "Maximum verification attempts exceeded."
            );
        }

        if (!passwordEncoder.matches(rawOtp, stored.getOtpHash())) {

            stored.setAttempts(stored.getAttempts() + 1);

            long remainingSeconds =
                    stored.getExpiresAt().getEpochSecond()
                            - Instant.now().getEpochSecond();

            if (remainingSeconds <= 0) {
                otpRedisTemplate.delete(otpKey);
                throw new OtpExpiredException("OTP expired.");
            }

            otpRedisTemplate.opsForValue().set(
                    otpKey,
                    stored,
                    remainingSeconds,
                    TimeUnit.SECONDS
            );

            throw new OtpInvalidException("Invalid OTP.");
        }

        otpRedisTemplate.delete(otpKey);
    }

    public boolean canRequestOtp(String email) {

        return !Boolean.TRUE.equals(
                stringRedisTemplate.hasKey(
                        OTP_LOCK_PREFIX + email
                )
        );
    }
}
