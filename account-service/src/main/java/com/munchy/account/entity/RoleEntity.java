package com.munchy.account.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Table("roles")
public class RoleEntity {
    @Id
    private Short id;
    private String name;
    private String description;
    private Instant createdAt;
}
