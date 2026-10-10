package com.carbontrack.user.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

/** Partial profile update: null fields are left unchanged. */
public record UpdateProfileRequest(
        @Size(max = 100) String displayName,
        @Valid PreferencesDto preferences) {
}
