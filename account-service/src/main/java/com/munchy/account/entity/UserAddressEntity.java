package com.munchy.account.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Table("user_addresses")
public class UserAddressEntity {
    @Id
    private UUID id;
    private UUID userId;
    private String label;
    private String recipientName;
    private String recipientPhone;
    private String addressLine1;
    private String addressLine2;
    private String landmark;
    private String locality;
    private String city;
    private String state;
    private String postalCode;
    private String countryCode;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String deliveryInstructions;
    @Column("is_default")
    private boolean defaultAddress;
    @Column("is_active")
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
}
