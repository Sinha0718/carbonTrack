package com.carbontrack.config;

import com.carbontrack.user.AuthProvider;
import com.carbontrack.user.Role;
import com.carbontrack.user.User;
import com.carbontrack.user.UserRepository;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Creates an ADMIN account from ADMIN_EMAIL / ADMIN_PASSWORD on startup if it does not exist yet. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${carbontrack.bootstrap-admin.username:admin}")
    private String username;

    @Value("${carbontrack.bootstrap-admin.email:}")
    private String email;

    @Value("${carbontrack.bootstrap-admin.password:}")
    private String password;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.isBlank() || userRepository.existsByEmailIgnoreCase(email)) {
            return;
        }
        User admin = new User();
        admin.setUsername(username);
        admin.setEmail(email.toLowerCase(Locale.ROOT));
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setDisplayName("Administrator");
        admin.setRole(Role.ADMIN);
        admin.setAuthProvider(AuthProvider.LOCAL);
        userRepository.save(admin);
        log.info("Created bootstrap admin user {}", admin.getEmail());
    }
}
