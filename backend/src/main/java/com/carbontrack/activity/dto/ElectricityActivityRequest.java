package com.carbontrack.activity.dto;

import com.carbontrack.activity.EnergySource;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ElectricityActivityRequest(
        @NotNull EnergySource energySource,
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false, message = "must be greater than 0")
        @DecimalMax(value = "100000", message = "must be at most 100000 kWh per entry")
        @Digits(integer = 6, fraction = 4)
        BigDecimal kwh,
        @PastOrPresent LocalDate logDate,
        @Size(max = 500) String notes) {
}
