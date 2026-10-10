package com.carbontrack.activity.dto;

import com.carbontrack.activity.MealType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

public record FoodActivityRequest(
        @NotNull MealType mealType,
        @NotNull
        @DecimalMin(value = "0.0", inclusive = false, message = "must be greater than 0")
        @DecimalMax(value = "50", message = "must be at most 50 servings per entry")
        @Digits(integer = 2, fraction = 2)
        BigDecimal servings,
        @PastOrPresent LocalDate logDate,
        @Size(max = 500) String notes) {
}
