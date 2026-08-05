package com.munchy.account.security;

import com.munchy.account.dto.user.AccountUserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class JwtTokenService {
    public static final String ISSUER = "munchy";
    public static final String ACCESS_TYPE = "access";
    public static final String REFRESH_TYPE = "refresh";

    private final JwtEncoder encoder;
    private final ReactiveJwtDecoder decoder;
    private final Duration accessLifetime;
    private final Duration refreshLifetime;

    public JwtTokenService(
            JwtEncoder encoder,
            ReactiveJwtDecoder decoder,
            @Value("${munchy.jwt.access-token-duration}") Duration accessLifetime,
            @Value("${munchy.jwt.refresh-token-duration}") Duration refreshLifetime) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.accessLifetime = accessLifetime;
        this.refreshLifetime = refreshLifetime;
    }

    public IssuedTokenPair issue(AccountUserResponse user, UUID sessionId) {
        Instant now = Instant.now();
        UUID accessId = UUID.randomUUID();
        UUID refreshId = UUID.randomUUID();
        Instant refreshExpiresAt = now.plus(refreshLifetime);
        return new IssuedTokenPair(
                encode(user, sessionId, ACCESS_TYPE, accessId, now, now.plus(accessLifetime)),
                encode(user, sessionId, REFRESH_TYPE, refreshId, now, refreshExpiresAt),
                refreshId,
                refreshExpiresAt
        );
    }

    public Mono<Jwt> validateRefresh(String rawToken) {
        return decoder.decode(rawToken)
                .filter(jwt -> REFRESH_TYPE.equals(jwt.getClaimAsString("token_type")))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Invalid refresh token type")));
    }

    public Duration accessLifetime() {
        return accessLifetime;
    }

    public Duration refreshLifetime() {
        return refreshLifetime;
    }

    private String encode(
            AccountUserResponse user,
            UUID sessionId,
            String tokenType,
            UUID jwtId,
            Instant issuedAt,
            Instant expiresAt) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(user.getId().toString())
                .id(jwtId.toString())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("sid", sessionId.toString())
                .claim("email", user.getEmail())
                .claim("roles", user.getRoles())
                .claim("token_type", tokenType)
                .build();
        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }
}
