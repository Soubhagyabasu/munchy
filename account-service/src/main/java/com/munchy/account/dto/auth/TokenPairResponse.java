package com.munchy.account.dto.auth;

import com.munchy.account.dto.user.AccountUserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenPairResponse {
    private final String accessToken;
    private final String refreshToken;
    private final long accessMaxAgeSeconds;
    private final long refreshMaxAgeSeconds;
    private final AccountUserResponse user;
}
