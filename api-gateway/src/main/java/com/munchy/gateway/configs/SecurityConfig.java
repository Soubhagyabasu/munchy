package com.munchy.gateway.configs;

import com.munchy.gateway.security.JwtClaims;
import com.munchy.gateway.security.OAuthLoginSuccessHandler;
import com.munchy.gateway.security.TokenCookieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.server.resource.web.server.authentication.ServerBearerTokenAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class SecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Value("${munchy.frontend-failure-url}")
    private String failureUrl;

    @Value("${munchy.frontend-success-url}")
    private String successUrl;

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(
            ServerHttpSecurity http,
            OAuthLoginSuccessHandler successHandler,
            ReactiveJwtDecoder accessJwtDecoder) {
        return http
                // SameSite=Lax cookies mitigate cross-site POSTs for this milestone. Add an explicit
                // CSRF token strategy before supporting same-site untrusted subdomains.
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(
                                "/api/v1/health",
                                "/actuator/health",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/api/v1/auth/refresh",
                                "/api/v1/auth/logout").permitAll()
                        .pathMatchers("/api/v1/orders/**").hasRole("CUSTOMER")
                        .anyExchange().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .authenticationSuccessHandler(successHandler)
                        .authenticationFailureHandler((exchange, exception) -> {
                            log.warn("Google OAuth2 authentication failed: {}", exception.getClass().getSimpleName());
                            var response = exchange.getExchange().getResponse();
                            response.setStatusCode(HttpStatus.FOUND);
                            response.getHeaders().setLocation(URI.create(failureUrl));
                            return response.setComplete();
                        }))
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(jwt -> jwt
                                .jwtDecoder(accessJwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .bearerTokenConverter(accessTokenConverter()))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((exchange, exception) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        })
                        .accessDeniedHandler((exchange, exception) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                            return exchange.getResponse().setComplete();
                        }))
                .build();
    }

    @Bean
    ReactiveJwtDecoder accessJwtDecoder(@Value("${munchy.jwt.secret}") String secret) {
        NimbusReactiveJwtDecoder decoder = NimbusReactiveJwtDecoder.withSecretKey(jwtSecret(secret))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
        OAuth2TokenValidator<Jwt> validator = new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(JwtClaims.ISSUER),
                new JwtClaimValidator<>("token_type", JwtClaims.ACCESS_TYPE::equals)
        );
        decoder.setJwtValidator(validator);
        return decoder;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                successUrlOrigin(),
                "http://localhost:4200",
                "http://127.0.0.1:4200"
        ).stream().distinct().toList());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private ServerAuthenticationConverter accessTokenConverter() {
        ServerBearerTokenAuthenticationConverter bearer = new ServerBearerTokenAuthenticationConverter();
        return exchange -> {
            String path = exchange.getRequest().getPath().value();
            if (path.equals("/api/v1/auth/refresh") || path.equals("/api/v1/auth/logout")) {
                return Mono.empty();
            }
            return Mono.justOrEmpty(exchange.getRequest().getCookies().getFirst(TokenCookieService.ACCESS_COOKIE))
                .map(HttpCookie::getValue)
                .filter(value -> !value.isBlank())
                .map(value -> (Authentication) new org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken(value))
                .switchIfEmpty(bearer.convert(exchange));
        };
    }

    private ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }

    private SecretKey jwtSecret(String secret) {
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("MUNCHY_JWT_SECRET must be at least 32 bytes");
        }
        return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    private String successUrlOrigin() {
        URI uri = URI.create(successUrl);
        int port = uri.getPort();
        return uri.getScheme() + "://" + uri.getHost() + (port < 0 ? "" : ":" + port);
    }
}
