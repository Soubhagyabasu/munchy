package com.munchy.gateway.accounts;

public record GoogleAccountLoginRequest(
        String googleSubject,
        String email,
        String name,
        String pictureUrl,
        boolean emailVerified,
        String ipAddress,
        String userAgent
) {
    @Override
    public String toString() {
        return "GoogleAccountLoginRequest[identity=[REDACTED], ipAddress=[REDACTED], userAgent=[REDACTED]]";
    }
}
