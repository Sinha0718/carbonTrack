package com.carbontrack.activity;

/**
 * An activity type that can be logged. Its {@link #name()} is the key used to look up
 * the matching row in the emission_factors table.
 */
public interface ActivityType {

    String name();

    ActivityCategory category();

    default String unit() {
        return category().getUnit();
    }
}
