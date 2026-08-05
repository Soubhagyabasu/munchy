package com.munchy.account.repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface RoleAssignmentRepository {
    Mono<Void> assign(UUID userId, Short roleId);
    Flux<String> findRoleNames(UUID userId);
}
