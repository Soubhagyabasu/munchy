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
@Table("users")
public class UserEntity {
    @Id
    private UUID id;
    private String email;
    private String name;
    private String phoneNumber;
    private String pictureUrl;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;
}
