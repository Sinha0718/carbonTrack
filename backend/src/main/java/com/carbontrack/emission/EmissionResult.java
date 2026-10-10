package com.carbontrack.emission;

import java.math.BigDecimal;

public record EmissionResult(EmissionFactor factor, BigDecimal co2eKg) {
}
