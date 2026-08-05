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
    private static final String REFRESH_COOKIE_PATH = "/api/v1/auth";
    private static final String LEGACY_REFRESH_COOKIE_PATH = "/api/v1/auth/refresh";

    private final boolean secure;
    private final String domain;

    public TokenCookieService(
            @Value("${munchy.cookie.secure}") boolean secure,
            @Value("${munchy.cookie.domain:}") String domain) {
        this.secure = secure;
        this.domain = domain;
    }

    public void setTokens(ServerWebExchange exchange, TokenPair tokens) {
        // Remove the earlier, narrower cookie path so browsers cannot send two
        // refresh cookies with the same name during this migration.
        exchange.getResponse().addCookie(cookie(
                REFRESH_COOKIE, "", LEGACY_REFRESH_COOKIE_PATH, Duration.ZERO));
        exchange.getResponse().addCookie(cookie(
                ACCESS_COOKIE,
                tokens.accessToken(),
                "/",
                Duration.ofSeconds(tokens.accessMaxAgeSeconds())));
        exchange.getResponse().addCookie(cookie(
                REFRESH_COOKIE,
                tokens.refreshToken(),
                REFRESH_COOKIE_PATH,
                Duration.ofSeconds(tokens.refreshMaxAgeSeconds())));
    }

    public void clearTokens(ServerWebExchange exchange) {
        exchange.getResponse().addCookie(cookie(ACCESS_COOKIE, "", "/", Duration.ZERO));
        exchange.getResponse().addCookie(cookie(REFRESH_COOKIE, "", REFRESH_COOKIE_PATH, Duration.ZERO));
        exchange.getResponse().addCookie(cookie(
                REFRESH_COOKIE, "", LEGACY_REFRESH_COOKIE_PATH, Duration.ZERO));
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
