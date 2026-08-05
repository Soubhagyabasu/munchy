package com.munchy.account.dto.address;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class CreateAddressRequest {
    @NotBlank @Size(max = 40)
    private String label;
    @NotBlank @Size(max = 150)
    private String recipientName;
    @NotBlank @Size(max = 30)
    private String recipientPhone;
    @NotBlank @Size(max = 255)
    private String addressLine1;
    @Size(max = 255)
    private String addressLine2;
    @Size(max = 255)
    private String landmark;
    @NotBlank @Size(max = 150)
    private String locality;
    @NotBlank @Size(max = 120)
    private String city;
    @NotBlank @Size(max = 120)
    private String state;
    @NotBlank @Size(max = 20)
    private String postalCode;
    @NotBlank @Pattern(regexp = "[A-Za-z]{2}")
    private String countryCode;
    @DecimalMin("-90.0") @DecimalMax("90.0")
    private BigDecimal latitude;
    @DecimalMin("-180.0") @DecimalMax("180.0")
    private BigDecimal longitude;
    @Size(max = 500)
    private String deliveryInstructions;
}
