package com.carbontrack.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.carbontrack.user.Role;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "test-secret-that-is-definitely-long-enough-123456";
    private static final Instant NOW = Instant.parse("2025-01-01T10:00:00Z");
    private final UserPrincipal principal = new UserPrincipal(42L, "a@b.com", "hash", Role.USER);

    @Test
    void generatedTokenRoundTripsUserId() {
        JwtService service = service(Clock.fixed(NOW, ZoneOffset.UTC));
        assertThat(service.extractUserId(service.generateToken(principal))).contains(42L);
    }

    @Test
    void expiredTokenIsRejected() {
        String token = service(Clock.fixed(NOW, ZoneOffset.UTC)).generateToken(principal);
        JwtService later = service(Clock.fixed(NOW.plus(Duration.ofHours(2)), ZoneOffset.UTC));
        assertThat(later.extractUserId(token)).isEmpty();
    }

    @Test
    void tokenSignedWithDifferentKeyIsRejected() {
        String token = service(Clock.fixed(NOW, ZoneOffset.UTC)).generateToken(principal);
        JwtService other = new JwtService(
                new JwtProperties("another-secret-that-is-also-long-enough-987654", Duration.ofHours(1)),
                Clock.fixed(NOW, ZoneOffset.UTC));
        assertThat(other.extractUserId(token)).isEmpty();
    }

    @Test
    void garbageTokenIsRejected() {
        assertThat(service(Clock.systemUTC()).extractUserId("not-a-jwt")).isEmpty();
    }

    @Test
    void shortSecretFailsFast() {
        assertThatThrownBy(() -> new JwtService(new JwtProperties("too-short", Duration.ofHours(1)), Clock.systemUTC()))
                .isInstanceOf(IllegalStateException.class);
    }

    private JwtService service(Clock clock) {
        return new JwtService(new JwtProperties(SECRET, Duration.ofHours(1)), clock);
    }
}
