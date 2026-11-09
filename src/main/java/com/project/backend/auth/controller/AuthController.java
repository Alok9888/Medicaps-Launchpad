package com.project.backend.auth.controller;

import com.project.backend.auth.dto.*;
import com.project.backend.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ---------- SIGNUP ----------
    @PostMapping("/signup")
    public ResponseEntity<OtpResponse> signup(@Valid @RequestBody SignupRequest req) {
        OtpResponse resp = authService.initiateSignup(req);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/signup/verify")
    public ResponseEntity<AuthResponse> verifySignup(@Valid @RequestBody VerifyOtpRequest req) {
        AuthResponse resp = authService.verifySignupOtp(req);
        return ResponseEntity.ok(resp);
    }

    // ---------- LOGIN ----------
    @PostMapping("/login")
    public ResponseEntity<OtpResponse> login(@Valid @RequestBody LoginRequest req) {
        OtpResponse resp = authService.initiateLogin(req);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/login/verify")
    public ResponseEntity<AuthResponse> verifyLogin(@Valid @RequestBody VerifyOtpRequest req) {
        AuthResponse resp = authService.verifyLoginOtp(req);
        return ResponseEntity.ok(resp);
    }

    // ---------- REFRESH ----------
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        AuthResponse resp = authService.refresh(token);
        return ResponseEntity.ok(resp);
    }

    // ---------- LOGOUT ----------
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        authService.logout(token);
        return ResponseEntity.noContent().build();
    }
}