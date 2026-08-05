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
@Table("user_identities")
public class UserIdentityEntity {
    @Id
    private UUID id;
    private UUID userId;
    private String provider;
    private String providerSubject;
    private String providerEmail;
    private boolean emailVerified;
    private Instant createdAt;
    private Instant lastLoginAt;
}
