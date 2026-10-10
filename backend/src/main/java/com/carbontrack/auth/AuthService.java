package com.carbontrack.auth;

import com.carbontrack.auth.dto.AuthResponse;
import com.carbontrack.auth.dto.LoginRequest;
import com.carbontrack.auth.dto.RegisterRequest;
import com.carbontrack.common.DuplicateResourceException;
import com.carbontrack.security.JwtService;
import com.carbontrack.security.UserPrincipal;
import com.carbontrack.user.AuthProvider;
import com.carbontrack.user.Role;
import com.carbontrack.user.User;
import com.carbontrack.user.UserRepository;
import com.carbontrack.user.dto.UserProfileResponse;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Email is already registered");
        }
        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new DuplicateResourceException("Username is already taken");
        }
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName());
        user.setRole(Role.USER);
        user.setAuthProvider(AuthProvider.LOCAL);
        User saved = userRepository.saveAndFlush(user);
        return issueToken(saved);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim(), request.password()));
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return issueToken(userRepository.findById(principal.getId()).orElseThrow());
    }

    private AuthResponse issueToken(User user) {
        String token = jwtService.generateToken(UserPrincipal.from(user));
        return AuthResponse.bearer(token, jwtService.getExpirationSeconds(), UserProfileResponse.from(user));
    }
}
