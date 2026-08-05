package com.munchy.account.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Table("refresh_tokens")
public class RefreshTokenEntity {
    @Id
    private UUID id;
    private UUID sessionId;
    private UUID jwtId;
    private String tokenHash;
    private UUID parentTokenId;
    private Instant issuedAt;
    private Instant expiresAt;
    private Instant usedAt;
    private Instant revokedAt;
    private String revokeReason;
}
