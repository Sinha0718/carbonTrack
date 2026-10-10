package com.carbontrack.emission.dto;

import com.carbontrack.activity.ActivityCategory;
import com.carbontrack.emission.EmissionFactor;
import java.math.BigDecimal;
import java.time.LocalDate;

public record EmissionFactorResponse(
        Long id,
        ActivityCategory category,
        String activityType,
        String unit,
        BigDecimal kgCo2ePerUnit,
        String source,
        LocalDate effectiveDate) {

    public static EmissionFactorResponse from(EmissionFactor factor) {
        return new EmissionFactorResponse(
                factor.getId(),
                factor.getCategory(),
                factor.getActivityType(),
                factor.getUnit(),
                factor.getKgCo2ePerUnit(),
                factor.getSource(),
                factor.getEffectiveDate());
    }
}
