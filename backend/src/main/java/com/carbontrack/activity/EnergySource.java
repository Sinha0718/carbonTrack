package com.carbontrack.activity;

public enum EnergySource implements ActivityType {
    GRID_AVERAGE,
    COAL,
    NATURAL_GAS,
    SOLAR,
    WIND,
    HYDRO,
    NUCLEAR;

    @Override
    public ActivityCategory category() {
        return ActivityCategory.ELECTRICITY;
    }
}
