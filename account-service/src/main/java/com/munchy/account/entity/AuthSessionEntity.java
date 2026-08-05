package com.munchy.account.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.net.InetAddress;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Table("auth_sessions")
public class AuthSessionEntity {
    @Id
    private UUID id;
    private UUID userId;
    private UUID identityId;
    private String provider;
    private InetAddress ipAddress;
    private String userAgent;
    private String deviceName;
    private Instant createdAt;
    private Instant lastUsedAt;
    private Instant expiresAt;
    private Instant revokedAt;
    private String revokeReason;
}
