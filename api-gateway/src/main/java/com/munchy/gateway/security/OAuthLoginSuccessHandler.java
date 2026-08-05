package com.munchy.gateway.security;

import com.munchy.gateway.accounts.AccountServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
public class OAuthLoginSuccessHandler implements ServerAuthenticationSuccessHandler {
    private static final Logger log = LoggerFactory.getLogger(OAuthLoginSuccessHandler.class);

    private final AccountServiceClient accounts;
    private final TokenCookieService cookies;
    private final String successUrl;
    private final String failureUrl;

    public OAuthLoginSuccessHandler(
            AccountServiceClient accounts,
            TokenCookieService cookies,
            @Value("${munchy.frontend-success-url}") String successUrl,
            @Value("${munchy.frontend-failure-url}") String failureUrl) {
        this.accounts = accounts;
        this.cookies = cookies;
        this.successUrl = successUrl;
        this.failureUrl = failureUrl;
    }

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange exchange, Authentication authentication) {
        OAuth2AuthenticationToken oauth = (OAuth2AuthenticationToken) authentication;
        return accounts.loginWithGoogle(oauth.getPrincipal(), exchange.getExchange())
                .flatMap(tokens -> {
                    cookies.setTokens(exchange.getExchange(), tokens);
                    return redirectAndInvalidate(exchange, successUrl);
                })
                .onErrorResume(error -> {
                    log.error("Account Service rejected OAuth login: {}", error.getClass().getSimpleName());
                    return redirectAndInvalidate(exchange, failureUrl);
                });
    }

    private Mono<Void> redirectAndInvalidate(WebFilterExchange exchange, String location) {
        exchange.getExchange().getResponse().setStatusCode(HttpStatus.FOUND);
        exchange.getExchange().getResponse().getHeaders().setLocation(URI.create(location));
        return exchange.getExchange().getSession()
                .flatMap(session -> session.invalidate())
                .then(exchange.getExchange().getResponse().setComplete());
    }
}
