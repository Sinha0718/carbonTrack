package com.carbontrack.activity.dto;

import com.carbontrack.activity.ActivityCategory;
import com.carbontrack.activity.ActivityLog;
import com.carbontrack.emission.EmissionFactor;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ActivityLogResponse(
        Long id,
        ActivityCategory category,
        String activityType,
        BigDecimal quantity,
        String unit,
        BigDecimal co2eKg,
        Long emissionFactorId,
        BigDecimal kgCo2ePerUnit,
        String factorSource,
        LocalDate logDate,
        String notes,
        Instant createdAt) {

    public static ActivityLogResponse from(ActivityLog log) {
        EmissionFactor factor = log.getEmissionFactor();
        return new ActivityLogResponse(
                log.getId(),
                log.getCategory(),
                log.getActivityType(),
                log.getQuantity(),
                log.getUnit(),
                log.getCo2eKg(),
                factor != null ? factor.getId() : null,
                factor != null ? factor.getKgCo2ePerUnit() : null,
                factor != null ? factor.getSource() : null,
                log.getLogDate(),
                log.getNotes(),
                log.getCreatedAt());
    }
}
