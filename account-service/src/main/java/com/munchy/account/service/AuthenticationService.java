package com.munchy.account.service;

import com.munchy.account.dto.auth.GoogleLoginRequest;
import com.munchy.account.dto.auth.TokenPairResponse;
import reactor.core.publisher.Mono;

public interface AuthenticationService {
    Mono<TokenPairResponse> loginWithGoogle(GoogleLoginRequest request);
    Mono<TokenPairResponse> refresh(String refreshToken);
    Mono<Void> logout(String refreshToken);
}
