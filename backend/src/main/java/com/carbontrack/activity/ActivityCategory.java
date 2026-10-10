package com.carbontrack.activity;

public enum ActivityCategory {
    TRANSPORT("km"),
    ELECTRICITY("kWh"),
    FOOD("serving"),
    SHOPPING("USD");

    private final String unit;

    ActivityCategory(String unit) {
        this.unit = unit;
    }

    public String getUnit() {
        return unit;
    }
}
