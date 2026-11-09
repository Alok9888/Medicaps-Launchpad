
package com.project.backend.auth.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        if (!HttpMethod.POST.matches(request.getMethod())) {
            return true;
        }

        String path = request.getServletPath();

        return !path.equals("/api/v1/auth/signup")
                && !path.equals("/api/v1/auth/login");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String ip = request.getRemoteAddr();

        String path = request.getServletPath();

        RateLimitService.Decision decision;

        if (path.equals("/api/v1/auth/signup")) {

            decision = rateLimitService.checkSignupIp(ip);

        } else {

            decision = rateLimitService.checkLoginIp(ip);
        }

        if (!decision.allowed()) {

            response.setStatus(
                    HttpStatus.TOO_MANY_REQUESTS.value()
            );

            response.setContentType(
                    MediaType.APPLICATION_JSON_VALUE
            );

            response.setHeader(
                    "Retry-After",
                    String.valueOf(decision.retryAfterSeconds())
            );

            response.getWriter().write(
                    """
                    {
                        "error": "Too many requests",
                        "message": "Please try again later.",
                        "retryAfterSeconds": %d
                    }
                    """.formatted(decision.retryAfterSeconds())
            );

            return;
        }

        response.setHeader(
                "X-Rate-Limit-Remaining",
                String.valueOf(decision.remainingTokens())
        );

        filterChain.doFilter(request, response);
    }
}
