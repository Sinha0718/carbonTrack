package com.carbontrack.user.dto;

import com.carbontrack.user.AuthProvider;
import com.carbontrack.user.Role;
import com.carbontrack.user.User;
import java.time.Instant;

public record UserProfileResponse(
        Long id,
        String username,
        String email,
        String displayName,
        Role role,
        AuthProvider authProvider,
        Long organisationId,
        PreferencesDto preferences,
        Instant createdAt) {

    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole(),
                user.getAuthProvider(),
                user.getOrganisation() != null ? user.getOrganisation().getId() : null,
                PreferencesDto.from(user.getPreferences()),
                user.getCreatedAt());
    }
}
