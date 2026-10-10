package com.carbontrack.activity.dto;

import com.carbontrack.activity.TransportMode;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TransportActivityRequest(
        @NotNull TransportMode mode,
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false, message = "must be greater than 0")
        @DecimalMax(value = "50000", message = "must be at most 50000 km per entry")
        @Digits(integer = 6, fraction = 4)
        BigDecimal distanceKm,
        @PastOrPresent LocalDate logDate,
        @Size(max = 500) String notes) {
}
