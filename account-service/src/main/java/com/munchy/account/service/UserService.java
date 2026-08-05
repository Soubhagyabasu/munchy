package com.munchy.account.service;

import com.munchy.account.dto.user.AccountUserResponse;
import com.munchy.account.dto.user.UpdateUserRequest;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserService {
    Mono<AccountUserResponse> getById(UUID userId);
    Mono<AccountUserResponse> update(UUID userId, UpdateUserRequest request);
}
