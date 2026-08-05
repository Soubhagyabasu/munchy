package com.munchy.account.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class UserRoleEntity {
    private UUID userId;
    private Short roleId;
    private Instant assignedAt;
}
