package com.munchy.gateway.configs;

import com.munchy.gateway.security.JwtService;
import com.munchy.gateway.users.LocalUser;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTests {
    private static final String SECRET = "test-only-secret-that-is-at-least-32-bytes-long";

    @Test
    void createsTypedAccessAndRefreshTokensForLocalUser() {
        SecurityConfig security = new SecurityConfig();
        JwtService service = new JwtService(
                security.jwtEncoder(SECRET),
                security.baseJwtDecoder(SECRET),
                Duration.ofMinutes(15),
                Duration.ofDays(7));
        LocalUser user = new LocalUser(
                "munchy-user-1",
                "google-123",
                "user@example.com",
                "Munchy User",
                null,
                List.of("ROLE_CUSTOMER"));

        var pair = service.createTokenPair(user);
        var access = security.baseJwtDecoder(SECRET).decode(pair.accessToken()).block();
        var refresh = service.validateRefreshToken(pair.refreshToken()).block();

        assertThat(access).isNotNull();
        assertThat(access.getSubject()).isEqualTo("munchy-user-1");
        assertThat(access.getClaimAsString("token_type")).isEqualTo("access");
        assertThat(access.getClaimAsStringList("roles")).containsExactly("ROLE_CUSTOMER");
        assertThat(refresh).isNotNull();
        assertThat(refresh.getClaimAsString("token_type")).isEqualTo("refresh");
    }

    @Test
    void refreshValidationRejectsAnAccessToken() {
        SecurityConfig security = new SecurityConfig();
        JwtService service = new JwtService(
                security.jwtEncoder(SECRET),
                security.baseJwtDecoder(SECRET),
                Duration.ofMinutes(15),
                Duration.ofDays(7));
        LocalUser user = new LocalUser("1", "g", "u@example.com", "User", null, List.of("ROLE_CUSTOMER"));

        assertThatThrownBy(() -> service.validateRefreshToken(
                service.createTokenPair(user).accessToken()).block())
                .isInstanceOf(IllegalArgumentException.class);
    }
}
