package com.carbontrack.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class UserPreferences {

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_units", nullable = false, length = 20)
    private UnitSystem preferredUnits = UnitSystem.METRIC;

    @Enumerated(EnumType.STRING)
    @Column(name = "goal_visibility", nullable = false, length = 20)
    private GoalVisibility goalVisibility = GoalVisibility.PRIVATE;

    @Column(name = "leaderboard_opt_in", nullable = false)
    private boolean leaderboardOptIn = true;

    @Column(name = "weekly_insights_enabled", nullable = false)
    private boolean weeklyInsightsEnabled = true;
}
