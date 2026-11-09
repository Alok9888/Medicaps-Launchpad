package com.project.backend.auth.config;


import com.project.backend.auth.ratelimit.RateLimitFilter;
import com.project.backend.auth.ratelimit.RateLimitService;
import com.project.backend.auth.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final RateLimitService rateLimitService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, RateLimitService rateLimitService) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/**").permitAll()
                        // Swagger / health‑check can be added here
                        .anyRequest().authenticated()
                )
                // Register the JWT filter FIRST; only then can another filter be positioned relative to it.
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new RateLimitFilter(rateLimitService), JwtAuthenticationFilter.class);

        return http.build();
    }

    // JwtAuthenticationFilter is a @Component, so Boot would ALSO register it as a plain servlet filter.
    // It would then run outside the security chain, mark itself "already filtered", and get skipped
    // inside the chain, so authentication would be lost (every protected endpoint -> 403). Disable that.
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> reg = new FilterRegistrationBean<>(filter);
        reg.setEnabled(false);
        return reg;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        // Used for BCrypt OTP hashing (not for user passwords)
        return new BCryptPasswordEncoder();
    }
}