package com.munchy.account.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class InternalRequestAuthenticationFilter implements WebFilter {
    public static final String HEADER = "X-Munchy-Internal-Key";

    private final byte[] expectedKey;

    public InternalRequestAuthenticationFilter(@Value("${munchy.jwt.secret}") String jwtSecret) {
        this.expectedKey = derive(jwtSecret).getBytes(StandardCharsets.US_ASCII);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (path.equals("/api/v1/account/health") || path.equals("/actuator/health")) {
            return chain.filter(exchange);
        }

        String supplied = exchange.getRequest().getHeaders().getFirst(HEADER);
        boolean accepted = supplied != null && MessageDigest.isEqual(
                expectedKey,
                supplied.getBytes(StandardCharsets.US_ASCII));
        if (!accepted) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
        return chain.filter(exchange);
    }

    private String derive(String jwtSecret) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest((jwtSecret + ":munchy-internal-service")
                            .getBytes(StandardCharsets.UTF_8)));
        }
        catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
