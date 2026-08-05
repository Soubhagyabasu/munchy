package com.munchy.gateway.security;

public record TokenPair(
        String accessToken,
        String refreshToken,
        long accessMaxAgeSeconds,
        long refreshMaxAgeSeconds
) {
    @Override
    public String toString() {
        return "TokenPair[accessToken=[REDACTED], refreshToken=[REDACTED], "
                + "accessMaxAgeSeconds=" + accessMaxAgeSeconds
                + ", refreshMaxAgeSeconds=" + refreshMaxAgeSeconds + "]";
    }
}
