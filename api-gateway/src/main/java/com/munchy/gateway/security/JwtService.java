package com.munchy.gateway.security;

import com.munchy.gateway.users.LocalUser;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class JwtService {
    public static final String ISSUER = "munchy";
    public static final String ACCESS_TYPE = "access";
    public static final String REFRESH_TYPE = "refresh";

    private final JwtEncoder encoder;
    private final ReactiveJwtDecoder baseDecoder;
    private final Duration accessLifetime;
    private final Duration refreshLifetime;

    public JwtService(
            JwtEncoder encoder,
            @Qualifier("baseJwtDecoder") ReactiveJwtDecoder baseDecoder,
            @Value("${munchy.jwt.access-token-duration}") Duration accessLifetime,
            @Value("${munchy.jwt.refresh-token-duration}") Duration refreshLifetime) {
        this.encoder = encoder;
        this.baseDecoder = baseDecoder;
        this.accessLifetime = accessLifetime;
        this.refreshLifetime = refreshLifetime;
    }

    public TokenPair createTokenPair(LocalUser user) {
        return new TokenPair(
                createToken(user, ACCESS_TYPE, accessLifetime),
                createToken(user, REFRESH_TYPE, refreshLifetime)
        );
    }

    public Mono<Jwt> validateRefreshToken(String token) {
        return baseDecoder.decode(token)
                .filter(jwt -> REFRESH_TYPE.equals(jwt.getClaimAsString("token_type")))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Invalid refresh token type")));
    }

    public Duration accessLifetime() {
        return accessLifetime;
    }

    public Duration refreshLifetime() {
        return refreshLifetime;
    }

    private String createToken(LocalUser user, String type, Duration lifetime) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(user.id())
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiresAt(now.plus(lifetime))
                .claim("email", user.email())
                .claim("roles", user.roles())
                .claim("token_type", type)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
