package com.munchy.account.dto.location;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class CurrentLocationRequest {
    @NotNull @DecimalMin("-90.0") @DecimalMax("90.0")
    private BigDecimal latitude;
    @NotNull @DecimalMin("-180.0") @DecimalMax("180.0")
    private BigDecimal longitude;
    @PositiveOrZero
    private BigDecimal accuracyMeters;
    @NotNull
    private Instant capturedAt;
}
