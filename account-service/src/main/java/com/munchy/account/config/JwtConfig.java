package com.munchy.account.config;

import com.munchy.account.security.JwtTokenService;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtConfig {
    @Bean
    public JwtEncoder jwtEncoder(@Value("${munchy.jwt.secret}") String secret) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecret(secret)));
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder(@Value("${munchy.jwt.secret}") String secret) {
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withSecretKey(jwtSecret(secret))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(JwtTokenService.ISSUER));
        return decoder;
    }

    private SecretKey jwtSecret(String secret) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("MUNCHY_JWT_SECRET must be at least 32 bytes");
        }
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }
}
