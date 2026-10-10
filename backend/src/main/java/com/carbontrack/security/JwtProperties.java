package com.carbontrack.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "carbontrack.jwt")
public record JwtProperties(String secret, Duration expiration) {
}
