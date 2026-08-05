package com.munchy.gateway.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class InternalServiceKey {
    public static final String HEADER = "X-Munchy-Internal-Key";

    private final String value;

    public InternalServiceKey(@Value("${munchy.jwt.secret}") String jwtSecret) {
        this.value = derive(jwtSecret);
    }

    public String value() {
        return value;
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
