package com.project.backend.auth.service;

import com.project.backend.auth.dto.*;
import com.project.backend.auth.dto.*;
import com.project.backend.auth.entity.*;
import com.project.backend.auth.exception.*;
import com.project.backend.auth.ratelimit.RateLimitService;
import com.project.backend.auth.repository.*;
import com.project.backend.auth.security.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final RateLimitService rateLimitService;
    private final UserRepository userRepo;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final RefreshTokenStore refreshTokenStore;
    private final PasswordEncoder passwordEncoder; // can be BCryptPasswordEncoder bean

    // ------------------- SIGNUP -------------------
    @Transactional
    public OtpResponse initiateSignup(SignupRequest req) {

        String email = req.getEmail()
                .toLowerCase(Locale.ROOT)
                .trim();

        // Validate college domain
        if (!email.endsWith("@medicaps.ac.in")) {
            throw new IllegalArgumentException(
                    "Only college emails are accepted."
            );
        }

        // Email-based rate limiting
        rateLimitService.checkSignupEmail(email);

        // Ensure user does NOT exist already
        if (userRepo.findByEmailIgnoreCase(email).isPresent()) {
            throw new IllegalArgumentException(
                    "User already registered. Use login."
            );
        }

        otpService.generateAndSendOtp(email, "SIGNUP");

        return new OtpResponse(
                "OTP sent to " + email,
                Long.parseLong(
                        System.getenv().getOrDefault(
                                "OTP_TTL_SECONDS", "300"
                        )
                )
        );
    }
    @Transactional
    public AuthResponse verifySignupOtp(VerifyOtpRequest req) {
        String email = req.getEmail().toLowerCase(Locale.ROOT).trim();
        otpService.verifyOtp(email, req.getOtp());

        // After successful OTP verification, create the user with default role
        Set<Role> defaultRole = Set.of(Role.STUDENT);

        // In a real app you would have a Role table seeded; for brevity we just use name.
        User user = User.builder()
                .email(email)
                .active(true)
                .role(defaultRole)
                .build();
        userRepo.save(user);

        // Issue tokens
        String access = jwtService.generateAccessToken(email, List.of("ROLE_STUDENT"));
        String refresh = jwtService.generateRefreshToken(email);
        return new AuthResponse(access, refresh);
    }

    // ------------------- LOGIN -------------------
    @Transactional
    public OtpResponse initiateLogin(LoginRequest req) {

        String email = req.getEmail()
                .toLowerCase(Locale.ROOT)
                .trim();

        // Email-based rate limiting
        rateLimitService.checkLoginEmail(email);

        // Verify account exists & active
        User user = userRepo.findByEmailIgnoreCase(email)
                .filter(User::isActive)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No active account for that email"
                        )
                );

        otpService.generateAndSendOtp(email, "LOGIN");

        return new OtpResponse(
                "OTP sent to " + email,
                Long.parseLong(
                        System.getenv().getOrDefault(
                                "OTP_TTL_SECONDS", "300"
                        )
                )
        );
    }

    @Transactional
    public AuthResponse verifyLoginOtp(VerifyOtpRequest req) {
        String email = req.getEmail().toLowerCase(Locale.ROOT).trim();
        otpService.verifyOtp(email, req.getOtp());

        User user = userRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found after OTP verification"));

        List<String> roleNames = user.getRole()
                .stream()
                .map(role -> "ROLE_" + role.name())
                .toList();

        String access = jwtService.generateAccessToken(email, roleNames);
        String refresh = jwtService.generateRefreshToken(email);
        return new AuthResponse(access, refresh);
    }

    // ------------------- REFRESH -------------------
    public AuthResponse refresh(String refreshToken) {
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }

        Jws<Claims> claims = jwtService.parseClaims(refreshToken);
        String jti = claims.getBody().getId();

        // Check revocation
        if (refreshTokenStore.isRevoked(jti)) {
            throw new IllegalArgumentException("Refresh token has been revoked");
        }

        String email = claims.getBody().getSubject();
        User user = userRepo.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<String> roles = user.getRole()
                .stream()
                .map(Role::name)
                .toList();

        String newAccess = jwtService.generateAccessToken(email, roles);
        // Do *not* rotate refresh token here (you could if you want)
        return new AuthResponse(newAccess, refreshToken);
    }

    // ------------------- LOGOUT -------------------
    public void logout(String refreshToken) {
        if (!jwtService.isTokenValid(refreshToken)) {
            // silently ignore – token is already unusable
            return;
        }
        Jws<Claims> claims = jwtService.parseClaims(refreshToken);
        String jti = claims.getBody().getId();
        long ttl = claims.getBody().getExpiration().getTime() - System.currentTimeMillis();
        refreshTokenStore.revoke(jti, ttl);
    }
}