package com.munchy.account.dto.address;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class AddressResponse {
    private UUID id;
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
    private boolean defaultAddress;
}
