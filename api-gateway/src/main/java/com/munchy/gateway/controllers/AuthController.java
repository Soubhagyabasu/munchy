package com.munchy.gateway.controllers;

import com.munchy.gateway.security.JwtService;
import com.munchy.gateway.security.TokenCookieService;
import com.munchy.gateway.users.LocalUser;
import com.munchy.gateway.users.UserAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final JwtService jwtService;
    private final TokenCookieService cookies;
    private final UserAccountService users;

    public AuthController(JwtService jwtService, TokenCookieService cookies, UserAccountService users) {
        this.jwtService = jwtService;
        this.cookies = cookies;
        this.users = users;
    }

    @GetMapping("/me")
    public Map<String, Object> currentUser(@AuthenticationPrincipal Jwt jwt) {
        LocalUser user = users.requireById(jwt.getSubject());
        return Map.of(
                "username", user.id(),
                "fullName", user.name(),
                "email", user.email(),
                "picture", user.picture() == null ? "" : user.picture(),
                "roles", user.roles()
        );
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<Map<String, String>>> refresh(ServerWebExchange exchange) {
        var cookie = exchange.getRequest().getCookies().getFirst(TokenCookieService.REFRESH_COOKIE);
        if (cookie == null || cookie.getValue().isBlank()) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        return jwtService.validateRefreshToken(cookie.getValue())
                .map(jwt -> users.requireById(jwt.getSubject()))
                .map(user -> {
                    cookies.setTokens(exchange, jwtService.createTokenPair(user), jwtService);
                    return ResponseEntity.ok(Map.of("status", "refreshed"));
                })
                .onErrorReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(ServerWebExchange exchange) {
        cookies.clearTokens(exchange);
        return ResponseEntity.noContent().build();
    }
}
