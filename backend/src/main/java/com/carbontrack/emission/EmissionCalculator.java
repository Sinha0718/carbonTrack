package com.carbontrack.emission;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import org.springframework.stereotype.Component;

/** Pure calculation rule: kg CO2e = quantity x kg CO2e per unit, rounded to 4 decimal places. */
@Component
public class EmissionCalculator {

    public static final int SCALE = 4;

    public BigDecimal calculate(BigDecimal quantity, BigDecimal kgCo2ePerUnit) {
        Objects.requireNonNull(quantity, "quantity must not be null");
        Objects.requireNonNull(kgCo2ePerUnit, "kgCo2ePerUnit must not be null");
        if (quantity.signum() < 0) {
            throw new IllegalArgumentException("Quantity must not be negative");
        }
        if (kgCo2ePerUnit.signum() < 0) {
            throw new IllegalArgumentException("Emission factor must not be negative");
        }
        return quantity.multiply(kgCo2ePerUnit).setScale(SCALE, RoundingMode.HALF_UP);
    }
}
