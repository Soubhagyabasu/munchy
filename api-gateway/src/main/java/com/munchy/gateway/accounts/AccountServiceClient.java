package com.munchy.gateway.accounts;

import com.munchy.gateway.security.TokenPair;
import com.munchy.gateway.security.InternalServiceKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

@Component
public class AccountServiceClient {
    private final WebClient client;

    public AccountServiceClient(
            @Value("${munchy.account-service-url}") String accountServiceUrl,
            InternalServiceKey internalServiceKey) {
        this.client = WebClient.builder()
                .baseUrl(accountServiceUrl)
                .defaultHeader(InternalServiceKey.HEADER, internalServiceKey.value())
                .build();
    }

    public Mono<TokenPair> loginWithGoogle(OAuth2User principal, ServerWebExchange exchange) {
        GoogleAccountLoginRequest request = new GoogleAccountLoginRequest(
                required(principal, "sub"),
                required(principal, "email"),
                required(principal, "name"),
                principal.getAttribute("picture"),
                Boolean.TRUE.equals(principal.<Boolean>getAttribute("email_verified")),
                remoteIp(exchange),
                exchange.getRequest().getHeaders().getFirst(HttpHeaders.USER_AGENT)
        );
        return client.post()
                .uri("/internal/v1/auth/oauth/google")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(TokenPair.class);
    }

    public Mono<TokenPair> refresh(String refreshToken) {
        return client.post()
                .uri("/internal/v1/auth/refresh")
                .bodyValue(new RefreshTokenRequest(refreshToken))
                .retrieve()
                .bodyToMono(TokenPair.class);
    }

    public Mono<Void> logout(String refreshToken) {
        return client.post()
                .uri("/internal/v1/auth/logout")
                .bodyValue(new RefreshTokenRequest(refreshToken))
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<AccountUser> findUser(String userId) {
        return client.get()
                .uri("/internal/v1/users/{userId}", userId)
                .retrieve()
                .bodyToMono(AccountUser.class);
    }

    private String required(OAuth2User principal, String claim) {
        String value = principal.getAttribute(claim);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Google principal is missing required claim: " + claim);
        }
        return value;
    }

    private String remoteIp(ServerWebExchange exchange) {
        InetSocketAddress address = exchange.getRequest().getRemoteAddress();
        return address == null ? null : address.getAddress().getHostAddress();
    }
}
