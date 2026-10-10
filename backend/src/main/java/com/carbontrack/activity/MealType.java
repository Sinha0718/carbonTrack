package com.carbontrack.activity;

public enum MealType implements ActivityType {
    BEEF,
    LAMB,
    PORK,
    CHICKEN,
    FISH,
    VEGETARIAN,
    VEGAN;

    @Override
    public ActivityCategory category() {
        return ActivityCategory.FOOD;
    }
}
