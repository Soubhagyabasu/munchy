package com.munchy.account.controller;

import com.munchy.account.dto.user.AccountUserResponse;
import com.munchy.account.dto.user.UpdateUserRequest;
import com.munchy.account.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/me")
public class UserController {
    private final UserService users;

    public UserController(UserService users) {
        this.users = users;
    }

    @GetMapping
    public Mono<AccountUserResponse> currentUser(@RequestHeader("X-User-Id") UUID userId) {
        return users.getById(userId);
    }

    @PatchMapping
    public Mono<AccountUserResponse> update(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody UpdateUserRequest request) {
        return users.update(userId, request);
    }
}
