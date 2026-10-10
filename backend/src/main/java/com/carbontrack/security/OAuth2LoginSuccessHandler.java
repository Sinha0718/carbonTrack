package com.carbontrack.security;

import com.carbontrack.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * After a successful Google login: find or create the local user, issue our own JWT and redirect
 * to the frontend with the token in the URL fragment (fragments are not sent to servers or logged).
 */
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtService jwtService;

    @Value("${carbontrack.oauth2.redirect-uri}")
    private String redirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");
        if (email == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Google account has no email address");
            return;
        }
        var user = userService.findOrCreateGoogleUser(email, oauthUser.getAttribute("name"), oauthUser.getName());
        String token = jwtService.generateToken(UserPrincipal.from(user));
        String target = UriComponentsBuilder.fromUriString(redirectUri).fragment("token=" + token).build().toUriString();
        response.sendRedirect(target);
    }
}
