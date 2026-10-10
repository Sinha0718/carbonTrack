package com.carbontrack.emission.dto;

import com.carbontrack.activity.ActivityCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateEmissionFactorRequest(
        @NotNull ActivityCategory category,
        @NotBlank @Size(max = 64) @Pattern(regexp = "^[A-Z0-9_]+$", message = "must be UPPER_SNAKE_CASE")
        String activityType,
        @NotBlank @Size(max = 32) String unit,
        @NotNull @DecimalMin("0.0") @Digits(integer = 6, fraction = 6) BigDecimal kgCo2ePerUnit,
        @NotBlank @Size(max = 255) String source,
        @NotNull LocalDate effectiveDate) {
}
