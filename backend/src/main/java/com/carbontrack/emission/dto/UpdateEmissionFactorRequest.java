package com.carbontrack.emission.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdateEmissionFactorRequest(
        @NotNull @DecimalMin("0.0") @Digits(integer = 6, fraction = 6) BigDecimal kgCo2ePerUnit,
        @NotBlank @Size(max = 255) String source) {
}
