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
public class AdminKeyFilter extends OncePerRequestFilter {

    @Value("${admin.api.key:}")
    private String adminApiKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Only protect /api/v1/admin/** endpoints
        if (path.startsWith("/api/v1/admin/")) {
            String providedKey = request.getHeader("X-Admin-Key");

            if (adminApiKey == null || adminApiKey.isBlank()) {
                // No key configured - deny all admin access
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"code\":\"ADMIN_DISABLED\",\"message\":\"Admin API is disabled\"}");
                return;
            }

            if (providedKey == null || !providedKey.equals(adminApiKey)) {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"code\":\"UNAUTHORIZED\",\"message\":\"Invalid or missing X-Admin-Key header\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}