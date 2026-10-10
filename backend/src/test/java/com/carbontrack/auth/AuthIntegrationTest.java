package com.carbontrack.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carbontrack.support.AbstractIntegrationTest;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

class AuthIntegrationTest extends AbstractIntegrationTest {

    @Test
    void registerLoginAndFetchProfile() throws Exception {
        String suffix = uniqueSuffix();
        String email = "Alice_" + suffix + "@Example.com";

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", "alice_" + suffix, "email", email,
                                "password", "Password123!", "displayName", "Alice"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value(email.toLowerCase()))
                .andExpect(jsonPath("$.user.role").value("USER"))
                .andExpect(jsonPath("$.user.preferences.preferredUnits").value("METRIC"));

        String login = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", "Password123!"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = readTree(login).get("accessToken").asText();

        mockMvc.perform(get("/api/users/me").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice_" + suffix))
                .andExpect(jsonPath("$.displayName").value("Alice"))
                .andExpect(jsonPath("$.authProvider").value("LOCAL"));
    }

    @Test
    void duplicateEmailIsRejected() throws Exception {
        String suffix = uniqueSuffix();
        var body = Map.of("username", "dup_" + suffix, "email", "dup_" + suffix + "@example.com",
                "password", "Password123!");
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(json(body)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", "other_" + suffix, "email", "DUP_" + suffix + "@example.com",
                                "password", "Password123!"))))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidRegistrationPayloadReturnsFieldErrors() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", "x", "email", "not-an-email", "password", "short"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.username").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());
    }

    @Test
    void wrongPasswordReturns401() throws Exception {
        String suffix = uniqueSuffix();
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", "bob_" + suffix, "email", "bob_" + suffix + "@example.com",
                                "password", "Password123!"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "bob_" + suffix + "@example.com", "password", "WrongPass1!"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointsRequireValidToken() throws Exception {
        mockMvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/users/me").header(HttpHeaders.AUTHORIZATION, bearer("invalid.token.value")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateProfilePreferencesPartially() throws Exception {
        String token = registerAndGetToken();

        mockMvc.perform(put("/api/users/me").header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"displayName": "Eco Hero",
                                 "preferences": {"preferredUnits": "IMPERIAL", "goalVisibility": "PUBLIC"}}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Eco Hero"))
                .andExpect(jsonPath("$.preferences.preferredUnits").value("IMPERIAL"))
                .andExpect(jsonPath("$.preferences.goalVisibility").value("PUBLIC"))
                .andExpect(jsonPath("$.preferences.leaderboardOptIn").value(true));

        mockMvc.perform(put("/api/users/me").header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"preferences\": {\"preferredUnits\": \"FURLONGS\"}}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePasswordRequiresCurrentPassword() throws Exception {
        String suffix = uniqueSuffix();
        String email = "pw_" + suffix + "@example.com";
        String response = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", "pw_" + suffix, "email", email, "password", "Password123!"))))
                .andReturn().getResponse().getContentAsString();
        String token = readTree(response).get("accessToken").asText();

        mockMvc.perform(put("/api/users/me/password").header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("currentPassword", "nope-nope", "newPassword", "NewPassword456!"))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put("/api/users/me/password").header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("currentPassword", "Password123!", "newPassword", "NewPassword456!"))))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", "NewPassword456!"))))
                .andExpect(status().isOk());
    }
}
