package com.munchy.gateway.security;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticatedUserHeadersFilter implements GlobalFilter, Ordered {
    private static final String USER_ID = "X-User-Id";
    private static final String USER_EMAIL = "X-User-Email";
    private static final String USER_ROLES = "X-User-Roles";
    private static final String AUTH_SESSION_ID = "X-Auth-Session-Id";
    private final InternalServiceKey internalServiceKey;

    public AuthenticatedUserHeadersFilter(InternalServiceKey internalServiceKey) {
        this.internalServiceKey = internalServiceKey;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var sanitized = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.remove(USER_ID);
                    headers.remove(USER_EMAIL);
                    headers.remove(USER_ROLES);
                    headers.remove(AUTH_SESSION_ID);
                    headers.remove(InternalServiceKey.HEADER);
                    headers.set(InternalServiceKey.HEADER, internalServiceKey.value());
                });

        return exchange.getPrincipal()
                .ofType(JwtAuthenticationToken.class)
                .map(authentication -> sanitized.headers(headers -> {
                    headers.set(USER_ID, authentication.getToken().getSubject());
                    headers.set(USER_EMAIL, authentication.getToken().getClaimAsString("email"));
                    headers.set(USER_ROLES, String.join(",", authentication.getToken().getClaimAsStringList("roles")));
                    String sessionId = authentication.getToken().getClaimAsString("sid");
                    if (sessionId != null && !sessionId.isBlank()) {
                        headers.set(AUTH_SESSION_ID, sessionId);
                    }
                }).build())
                .defaultIfEmpty(sanitized.build())
                .flatMap(request -> chain.filter(exchange.mutate().request(request).build()));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
