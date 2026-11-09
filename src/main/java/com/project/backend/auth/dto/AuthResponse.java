package com.project.backend.auth.dto;

import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
}
