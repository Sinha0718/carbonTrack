package com.carbontrack.user;

import com.carbontrack.common.ResourceNotFoundException;
import com.carbontrack.user.dto.ChangePasswordRequest;
import com.carbontrack.user.dto.PreferencesDto;
import com.carbontrack.user.dto.UpdateProfileRequest;
import com.carbontrack.user.dto.UserProfileResponse;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        return UserProfileResponse.from(findById(userId));
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = findById(userId);
        if (request.displayName() != null) {
            user.setDisplayName(request.displayName().isBlank() ? null : request.displayName().trim());
        }
        PreferencesDto prefs = request.preferences();
        if (prefs != null) {
            UserPreferences current = user.getPreferences();
            if (prefs.preferredUnits() != null) {
                current.setPreferredUnits(prefs.preferredUnits());
            }
            if (prefs.goalVisibility() != null) {
                current.setGoalVisibility(prefs.goalVisibility());
            }
            if (prefs.leaderboardOptIn() != null) {
                current.setLeaderboardOptIn(prefs.leaderboardOptIn());
            }
            if (prefs.weeklyInsightsEnabled() != null) {
                current.setWeeklyInsightsEnabled(prefs.weeklyInsightsEnabled());
            }
        }
        return UserProfileResponse.from(userRepository.saveAndFlush(user));
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = findById(userId);
        // Users who signed up with Google have no password yet and may set one directly.
        if (user.getPasswordHash() != null
                && (request.currentPassword() == null
                || !passwordEncoder.matches(request.currentPassword(), user.getPasswordHash()))) {
            throw new BadCredentialsException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    @Transactional
    public User findOrCreateGoogleUser(String email, String name, String googleSubject) {
        return userRepository.findByEmailIgnoreCase(email).orElseGet(() -> {
            User user = new User();
            user.setEmail(email.toLowerCase(Locale.ROOT));
            user.setUsername(generateUniqueUsername(email));
            user.setDisplayName(name);
            user.setAuthProvider(AuthProvider.GOOGLE);
            user.setProviderId(googleSubject);
            return userRepository.save(user);
        });
    }

    private String generateUniqueUsername(String email) {
        String base = email.substring(0, email.indexOf('@')).replaceAll("[^a-zA-Z0-9_.-]", "");
        if (base.length() < 3) {
            base = "user" + base;
        }
        base = base.length() > 40 ? base.substring(0, 40) : base;
        String candidate = base;
        int suffix = 1;
        while (userRepository.existsByUsernameIgnoreCase(candidate)) {
            candidate = base + suffix++;
        }
        return candidate;
    }

    private User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User " + userId + " not found"));
    }
}
