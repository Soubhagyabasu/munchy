package com.munchy.account.service.impl;

import com.munchy.account.dto.user.AccountUserResponse;
import com.munchy.account.dto.user.UpdateUserRequest;
import com.munchy.account.entity.UserEntity;
import com.munchy.account.exception.ResourceNotFoundException;
import com.munchy.account.mapper.UserMapper;
import com.munchy.account.repository.RoleAssignmentRepository;
import com.munchy.account.repository.UserRepository;
import com.munchy.account.service.UserService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository users;
    private final RoleAssignmentRepository roleAssignments;
    private final UserMapper mapper;

    public UserServiceImpl(
            UserRepository users,
            RoleAssignmentRepository roleAssignments,
            UserMapper mapper) {
        this.users = users;
        this.roleAssignments = roleAssignments;
        this.mapper = mapper;
    }

    @Override
    public Mono<AccountUserResponse> getById(UUID userId) {
        return requireUser(userId).flatMap(this::toResponse);
    }

    @Override
    public Mono<AccountUserResponse> update(UUID userId, UpdateUserRequest request) {
        return requireUser(userId)
                .flatMap(user -> {
                    if (request.getName() != null) {
                        user.setName(request.getName().trim());
                    }
                    if (request.getPhoneNumber() != null) {
                        user.setPhoneNumber(request.getPhoneNumber().trim());
                    }
                    user.setUpdatedAt(Instant.now());
                    return users.save(user);
                })
                .flatMap(this::toResponse);
    }

    private Mono<UserEntity> requireUser(UUID userId) {
        return users.findById(userId)
                .filter(user -> !"DELETED".equals(user.getStatus()))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Munchy user was not found")));
    }

    private Mono<AccountUserResponse> toResponse(UserEntity user) {
        return roleAssignments.findRoleNames(user.getId())
                .map(role -> "ROLE_" + role)
                .collectList()
                .map(roles -> mapper.toResponse(user, null, roles));
    }
}
