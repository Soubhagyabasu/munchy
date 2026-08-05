package com.munchy.account.controller;

import com.munchy.account.dto.auth.GoogleLoginRequest;
import com.munchy.account.dto.auth.LogoutRequest;
import com.munchy.account.dto.auth.RefreshRequest;
import com.munchy.account.dto.auth.TokenPairResponse;
import com.munchy.account.dto.user.AccountUserResponse;
import com.munchy.account.service.AuthenticationService;
import com.munchy.account.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/internal/v1")
public class InternalAuthController {
    private final AuthenticationService authentication;
    private final UserService users;

    public InternalAuthController(AuthenticationService authentication, UserService users) {
        this.authentication = authentication;
        this.users = users;
    }

    @PostMapping("/auth/oauth/google")
    public Mono<TokenPairResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        return authentication.loginWithGoogle(request);
    }

    @PostMapping("/auth/refresh")
    public Mono<TokenPairResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return authentication.refresh(request.getRefreshToken());
    }

    @PostMapping("/auth/logout")
    public Mono<ResponseEntity<Void>> logout(@Valid @RequestBody LogoutRequest request) {
        return authentication.logout(request.getRefreshToken())
                .thenReturn(ResponseEntity.noContent().build());
    }

    @GetMapping("/users/{userId}")
    public Mono<AccountUserResponse> user(@PathVariable UUID userId) {
        return users.getById(userId);
    }
}
