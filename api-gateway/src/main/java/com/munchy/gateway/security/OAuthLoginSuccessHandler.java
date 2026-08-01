package com.munchy.gateway.security;

import com.munchy.gateway.users.LocalUser;
import com.munchy.gateway.users.UserAccountService;
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
    private final UserAccountService users;
    private final JwtService jwtService;
    private final TokenCookieService cookies;
    private final String successUrl;

    public OAuthLoginSuccessHandler(
            UserAccountService users,
            JwtService jwtService,
            TokenCookieService cookies,
            @Value("${munchy.frontend-success-url}") String successUrl) {
        this.users = users;
        this.jwtService = jwtService;
        this.cookies = cookies;
        this.successUrl = successUrl;
    }

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange exchange, Authentication authentication) {
        OAuth2AuthenticationToken oauth = (OAuth2AuthenticationToken) authentication;
        LocalUser user = users.findOrCreate(oauth.getPrincipal());
        cookies.setTokens(exchange.getExchange(), jwtService.createTokenPair(user), jwtService);
        exchange.getExchange().getResponse().setStatusCode(HttpStatus.FOUND);
        exchange.getExchange().getResponse().getHeaders().setLocation(URI.create(successUrl));
        return exchange.getExchange().getSession()
                .flatMap(session -> session.invalidate())
                .then(exchange.getExchange().getResponse().setComplete());
    }
}
