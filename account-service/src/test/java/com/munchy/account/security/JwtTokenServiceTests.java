package com.munchy.account.security;

import com.munchy.account.config.JwtConfig;
import com.munchy.account.dto.user.AccountUserResponse;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenServiceTests {
    private static final String SECRET = "test-only-secret-that-is-at-least-32-bytes-long";

    @Test
    void issuesAccessAndRefreshTokensBoundToAStableSession() {
        JwtConfig config = new JwtConfig();
        JwtTokenService service = new JwtTokenService(
                config.jwtEncoder(SECRET),
                config.jwtDecoder(SECRET),
                Duration.ofMinutes(15),
                Duration.ofDays(7));
        AccountUserResponse user = new AccountUserResponse();
        user.setId(UUID.randomUUID());
        user.setEmail("user@example.com");
        user.setRoles(List.of("ROLE_CUSTOMER"));
        UUID sessionId = UUID.randomUUID();

        IssuedTokenPair tokens = service.issue(user, sessionId);
        var refresh = service.validateRefresh(tokens.refreshToken()).block();

        assertThat(refresh).isNotNull();
        assertThat(refresh.getSubject()).isEqualTo(user.getId().toString());
        assertThat(refresh.getClaimAsString("sid")).isEqualTo(sessionId.toString());
        assertThat(refresh.getClaimAsString("token_type")).isEqualTo("refresh");
        assertThat(refresh.getId()).isEqualTo(tokens.refreshJwtId().toString());
    }
}
