package com.munchy.gateway.accounts;

public final class RefreshTokenRequest {
    private final String refreshToken;

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    @Override
    public String toString() {
        return "RefreshTokenRequest[refreshToken=[REDACTED]]";
    }
}
