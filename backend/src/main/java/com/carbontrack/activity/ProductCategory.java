package com.carbontrack.activity;

public enum ProductCategory implements ActivityType {
    CLOTHING,
    ELECTRONICS,
    FURNITURE,
    HOUSEHOLD_GOODS,
    BOOKS_AND_MEDIA,
    PERSONAL_CARE,
    TOYS_AND_SPORTS;

    @Override
    public ActivityCategory category() {
        return ActivityCategory.SHOPPING;
    }
}
