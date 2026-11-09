
package com.project.backend.auth.ratelimit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitProperties {

    // Signup IP limit
    private long signupIpLimit = 20;
    private Duration signupIpWindow = Duration.ofMinutes(10);

    // Login IP limit
    private long loginIpLimit = 30;
    private Duration loginIpWindow = Duration.ofMinutes(10);

    // Signup email limit
    private long signupEmailLimit = 5;
    private Duration signupEmailWindow = Duration.ofMinutes(10);

    // Login email limit
    private long loginEmailLimit = 10;
    private Duration loginEmailWindow = Duration.ofMinutes(10);
}

