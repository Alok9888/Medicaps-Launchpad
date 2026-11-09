package com.project.backend.auth.security;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RefreshTokenStore {

   // private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate redisTemplate;

    private static final String REVOKED_PREFIX = "revoked-refresh:";

    public void revoke(String jti, long ttlMillis) {
        String key = REVOKED_PREFIX + jti;
        redisTemplate.opsForValue().set(key, "revoked", Duration.ofMillis(ttlMillis));
    }

    public boolean isRevoked(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(REVOKED_PREFIX + jti));
    }
}
