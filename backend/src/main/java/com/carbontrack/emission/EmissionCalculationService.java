package com.carbontrack.emission;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Emission calculation engine: looks up the factor configured for an activity type/unit
 * on the activity date and applies it to the logged quantity.
 */
@Service
@RequiredArgsConstructor
public class EmissionCalculationService {

    private final EmissionFactorRepository emissionFactorRepository;
    private final EmissionCalculator emissionCalculator;

    @Transactional(readOnly = true)
    public EmissionResult calculate(String activityType, String unit, BigDecimal quantity, LocalDate onDate) {
        Objects.requireNonNull(onDate, "onDate must not be null");
        if (activityType == null || activityType.isBlank() || unit == null || unit.isBlank()) {
            throw new UnknownEmissionFactorException(activityType, unit, onDate);
        }
        EmissionFactor factor = emissionFactorRepository
                .findFirstByActivityTypeAndUnitAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
                        activityType, unit, onDate)
                .orElseThrow(() -> new UnknownEmissionFactorException(activityType, unit, onDate));
        BigDecimal co2eKg = emissionCalculator.calculate(quantity, factor.getKgCo2ePerUnit());
        return new EmissionResult(factor, co2eKg);
    }
}
