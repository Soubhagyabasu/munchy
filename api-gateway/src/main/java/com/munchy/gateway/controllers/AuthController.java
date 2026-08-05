package com.munchy.gateway.controllers;

import com.munchy.gateway.accounts.AccountServiceClient;
import com.munchy.gateway.security.TokenCookieService;
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
    private final TokenCookieService cookies;
    private final AccountServiceClient accounts;

    public AuthController(TokenCookieService cookies, AccountServiceClient accounts) {
        this.cookies = cookies;
        this.accounts = accounts;
    }

    @GetMapping("/me")
    public Mono<Map<String, Object>> currentUser(@AuthenticationPrincipal Jwt jwt) {
        return accounts.findUser(jwt.getSubject()).map(user -> Map.of(
                "username", user.id(),
                "fullName", user.name(),
                "email", user.email(),
                "picture", user.pictureUrl() == null ? "" : user.pictureUrl(),
                "roles", user.roles()
        ));
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<Map<String, String>>> refresh(ServerWebExchange exchange) {
        var cookie = exchange.getRequest().getCookies().getFirst(TokenCookieService.REFRESH_COOKIE);
        if (cookie == null || cookie.getValue().isBlank()) {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }

        return accounts.refresh(cookie.getValue())
                .map(tokens -> {
                    cookies.setTokens(exchange, tokens);
                    return ResponseEntity.ok(Map.of("status", "refreshed"));
                })
                .onErrorReturn(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<Void>> logout(ServerWebExchange exchange) {
        var cookie = exchange.getRequest().getCookies().getFirst(TokenCookieService.REFRESH_COOKIE);
        Mono<Void> revocation = cookie == null || cookie.getValue().isBlank()
                ? Mono.empty()
                : accounts.logout(cookie.getValue()).onErrorResume(error -> Mono.empty());
        return revocation.then(Mono.fromSupplier(() -> {
            cookies.clearTokens(exchange);
            return ResponseEntity.noContent().build();
        }));
    }
}
