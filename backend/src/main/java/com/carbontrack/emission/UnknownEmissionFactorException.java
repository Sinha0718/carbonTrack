package com.carbontrack.emission;

import java.time.LocalDate;

public class UnknownEmissionFactorException extends RuntimeException {

    public UnknownEmissionFactorException(String activityType, String unit, LocalDate date) {
        super("No emission factor configured for activity type '%s' (unit '%s') on %s"
                .formatted(activityType, unit, date));
    }
}
