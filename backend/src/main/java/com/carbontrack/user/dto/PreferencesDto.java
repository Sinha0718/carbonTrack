package com.carbontrack.user.dto;

import com.carbontrack.user.GoalVisibility;
import com.carbontrack.user.UnitSystem;
import com.carbontrack.user.UserPreferences;

/** Sustainability preferences. On update, null fields are left unchanged. */
public record PreferencesDto(
        UnitSystem preferredUnits,
        GoalVisibility goalVisibility,
        Boolean leaderboardOptIn,
        Boolean weeklyInsightsEnabled) {

    public static PreferencesDto from(UserPreferences prefs) {
        return new PreferencesDto(prefs.getPreferredUnits(), prefs.getGoalVisibility(),
                prefs.isLeaderboardOptIn(), prefs.isWeeklyInsightsEnabled());
    }
}
