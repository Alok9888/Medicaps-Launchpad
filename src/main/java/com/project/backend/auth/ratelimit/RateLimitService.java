
package com.project.backend.auth.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.api.StatefulRedisConnection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

@Service
@Slf4j
public class RateLimitService {

    private final RateLimitProperties properties;
    private final LettuceBasedProxyManager<String> proxyManager;

    public RateLimitService(
            RateLimitProperties properties,
            StatefulRedisConnection<String, byte[]> redisConnection
    ) {
        this.properties = properties;

        this.proxyManager = Bucket4jLettuce
                .casBasedBuilder(redisConnection)
                .maxRetries(5)
                .requestTimeout(Duration.ofSeconds(2))
                .expirationAfterWrite(
                        ExpirationAfterWriteStrategy
                                .basedOnTimeForRefillingBucketUpToMax(
                                        Duration.ofMinutes(1)
                                )
                )
                .build();
    }

    // ================= IP RATE LIMITING =================

    public Decision checkSignupIp(String ip) {

        return consume(
                "rl:signup:ip:" + hash(ip),
                properties.getSignupIpLimit(),
                properties.getSignupIpWindow()
        );
    }

    public Decision checkLoginIp(String ip) {

        return consume(
                "rl:login:ip:" + hash(ip),
                properties.getLoginIpLimit(),
                properties.getLoginIpWindow()
        );
    }

    // ================= EMAIL RATE LIMITING =================

    public void checkSignupEmail(String email) {

        Decision decision = consume(
                "rl:signup:email:" + hash(normalizeEmail(email)),
                properties.getSignupEmailLimit(),
                properties.getSignupEmailWindow()
        );

        rejectIfLimited(decision);
    }

    public void checkLoginEmail(String email) {

        Decision decision = consume(
                "rl:login:email:" + hash(normalizeEmail(email)),
                properties.getLoginEmailLimit(),
                properties.getLoginEmailWindow()
        );

        rejectIfLimited(decision);
    }

    // ================= BUCKET CREATION =================

    private Decision consume(
            String key,
            long capacity,
            Duration refillDuration
    ) {

        BucketConfiguration configuration =
                BucketConfiguration.builder()
                        .addLimit(limit -> limit
                                .capacity(capacity)
                                .refillGreedy(
                                        capacity,
                                        refillDuration
                                )
                        )
                        .build();

        Bucket bucket = proxyManager.getProxy(
                key,
                () -> configuration
        );

        ConsumptionProbe probe =
                bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {

            return new Decision(
                    true,
                    probe.getRemainingTokens(),
                    0
            );
        }

        long nanos = probe.getNanosToWaitForRefill();

        long retryAfterSeconds =
                (nanos + 999_999_999L) / 1_000_000_000L;

        return new Decision(
                false,
                probe.getRemainingTokens(),
                Math.max(1, retryAfterSeconds)
        );
    }

    // ================= HELPER METHODS =================

    private void rejectIfLimited(Decision decision) {

        if (!decision.allowed()) {
            throw new RateLimitExceededException(
                    decision.retryAfterSeconds()
            );
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private String hash(String value) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] encoded = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(encoded);

        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                    "SHA-256 algorithm unavailable", ex
            );
        }
    }

    public record Decision(
            boolean allowed,
            long remainingTokens,
            long retryAfterSeconds
    ) {}
}
