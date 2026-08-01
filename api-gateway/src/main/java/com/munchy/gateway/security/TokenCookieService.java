package com.munchy.gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import java.time.Duration;

@Component
public class TokenCookieService {
    public static final String ACCESS_COOKIE = "munchy_access_token";
    public static final String REFRESH_COOKIE = "munchy_refresh_token";

    private final boolean secure;
    private final String domain;

    public TokenCookieService(
            @Value("${munchy.cookie.secure}") boolean secure,
            @Value("${munchy.cookie.domain:}") String domain) {
        this.secure = secure;
        this.domain = domain;
    }

    public void setTokens(ServerWebExchange exchange, TokenPair tokens, JwtService jwtService) {
        exchange.getResponse().addCookie(cookie(ACCESS_COOKIE, tokens.accessToken(), "/", jwtService.accessLifetime()));
        exchange.getResponse().addCookie(cookie(
                REFRESH_COOKIE,
                tokens.refreshToken(),
                "/api/v1/auth/refresh",
                jwtService.refreshLifetime()));
    }

    public void clearTokens(ServerWebExchange exchange) {
        exchange.getResponse().addCookie(cookie(ACCESS_COOKIE, "", "/", Duration.ZERO));
        exchange.getResponse().addCookie(cookie(REFRESH_COOKIE, "", "/api/v1/auth/refresh", Duration.ZERO));
    }

    private ResponseCookie cookie(String name, String value, String path, Duration maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Lax")
                .path(path)
                .maxAge(maxAge);
        if (!domain.isBlank()) {
            builder.domain(domain);
        }
        return builder.build();
    }
}
