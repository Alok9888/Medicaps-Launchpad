package com.project.backend.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class InternalKeyFilter extends OncePerRequestFilter {

    @Value("${internal.api.key:}")
    private String internalApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (internalApiKey == null || internalApiKey.isBlank()) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"code\":\"INTERNAL_API_DISABLED\",\"message\":\"Internal API is disabled\"}");
            return;
        }

        String providedKey = request.getHeader("X-Internal-Key");
        if (!internalApiKey.equals(providedKey)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"code\":\"UNAUTHORIZED\",\"message\":\"Invalid or missing X-Internal-Key header\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
