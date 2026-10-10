package com.carbontrack.activity;

public enum TransportMode implements ActivityType {
    CAR_PETROL,
    CAR_DIESEL,
    CAR_HYBRID,
    CAR_ELECTRIC,
    MOTORBIKE,
    BUS,
    TRAIN,
    METRO,
    FLIGHT_DOMESTIC,
    FLIGHT_SHORT_HAUL,
    FLIGHT_LONG_HAUL,
    BICYCLE,
    WALK;

    @Override
    public ActivityCategory category() {
        return ActivityCategory.TRANSPORT;
    }
}
