package com.carbontrack.activity.dto;

import com.carbontrack.activity.ProductCategory;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ShoppingActivityRequest(
        @NotNull ProductCategory productCategory,
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false, message = "must be greater than 0")
        @DecimalMax(value = "1000000", message = "must be at most 1000000 USD per entry")
        @Digits(integer = 7, fraction = 2)
        BigDecimal spendAmount,
        @PastOrPresent LocalDate logDate,
        @Size(max = 500) String notes) {
}
