package com.carbontrack.emission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carbontrack.activity.ActivityCategory;
import com.carbontrack.activity.EnergySource;
import com.carbontrack.activity.MealType;
import com.carbontrack.activity.ProductCategory;
import com.carbontrack.activity.TransportMode;
import com.carbontrack.support.AbstractIntegrationTest;
import com.carbontrack.user.Role;
import com.carbontrack.user.User;
import com.carbontrack.user.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

class EmissionFactorIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private EmissionCalculationService emissionCalculationService;

    @Autowired
    private EmissionFactorRepository emissionFactorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /** Verifies the seeded (Flyway V2) factors against a table of known inputs and expected outputs. */
    @ParameterizedTest(name = "{0}: {2} {1} -> {3} kg CO2e")
    @CsvSource({
            "CAR_PETROL,        km,      100,     17.0000",
            "CAR_ELECTRIC,      km,      100,     4.7000",
            "FLIGHT_DOMESTIC,   km,      800,     196.8000",
            "WALK,              km,      5,       0.0000",
            "GRID_AVERAGE,      kWh,     250,     93.2500",
            "COAL,              kWh,     1000,    820.0000",
            "WIND,              kWh,     1000,    11.0000",
            "BEEF,              serving, 2,       15.0000",
            "CHICKEN,           serving, 4,       3.0000",
            "VEGAN,             serving, 3,       0.9000",
            "CLOTHING,          USD,     49.95,   12.9870",
            "HOUSEHOLD_GOODS,   USD,     100,     38.0000"
    })
    void seededFactorsProduceExpectedEmissions(String type, String unit, BigDecimal quantity, BigDecimal expected) {
        EmissionResult result = emissionCalculationService.calculate(type, unit, quantity, LocalDate.of(2025, 1, 1));
        assertThat(result.co2eKg()).isEqualByComparingTo(expected);
    }

    @Test
    void everyLoggableActivityTypeHasASeededFactor() {
        Stream.of(TransportMode.values(), EnergySource.values(), MealType.values(), ProductCategory.values())
                .flatMap(Arrays::stream)
                .forEach(type -> assertThat(emissionFactorRepository
                        .findFirstByActivityTypeAndUnitAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
                                type.name(), type.unit(), LocalDate.now()))
                        .as("factor for %s/%s", type.name(), type.unit())
                        .isPresent());
    }

    @Test
    void unknownActivityTypeIsRejected() {
        assertThatThrownBy(() -> emissionCalculationService.calculate("SPACE_SHUTTLE", "km", BigDecimal.ONE,
                LocalDate.now())).isInstanceOf(UnknownEmissionFactorException.class);
    }

    @Test
    void adminCanPublishNewFactorVersionWithoutAffectingHistoricalLogs() throws Exception {
        String admin = adminToken();
        String user = registerAndGetToken();
        LocalDate today = LocalDate.now();

        mockMvc.perform(post("/api/activities/transport").header(HttpHeaders.AUTHORIZATION, bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mode\":\"TRAIN\",\"distanceKm\":100,\"logDate\":\"" + today.minusDays(1) + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.co2eKg").value(3.5));

        mockMvc.perform(post("/api/admin/emission-factors").header(HttpHeaders.AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "category", ActivityCategory.TRANSPORT,
                                "activityType", "TRAIN",
                                "unit", "km",
                                "kgCo2ePerUnit", new BigDecimal("0.030000"),
                                "source", "Revised national rail factor",
                                "effectiveDate", today.toString()))))
                .andExpect(status().isCreated());

        // New entries use the revised factor; entries dated before it keep the old one.
        mockMvc.perform(post("/api/activities/transport").header(HttpHeaders.AUTHORIZATION, bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mode\":\"TRAIN\",\"distanceKm\":100}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.co2eKg").value(3.0))
                .andExpect(jsonPath("$.factorSource").value("Revised national rail factor"));
        mockMvc.perform(post("/api/activities/transport").header(HttpHeaders.AUTHORIZATION, bearer(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mode\":\"TRAIN\",\"distanceKm\":100,\"logDate\":\"" + today.minusDays(1) + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.co2eKg").value(3.5));

        mockMvc.perform(get("/api/emission-factors").param("category", "TRANSPORT")
                        .header(HttpHeaders.AUTHORIZATION, bearer(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.activityType == 'TRAIN')].kgCo2ePerUnit").value(0.03));

        // Same type/unit/date again -> conflict
        mockMvc.perform(post("/api/admin/emission-factors").header(HttpHeaders.AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("category", "TRANSPORT", "activityType", "TRAIN", "unit", "km",
                                "kgCo2ePerUnit", 0.02, "source", "dup", "effectiveDate", today.toString()))))
                .andExpect(status().isConflict());
    }

    @Test
    void adminCanCorrectAnExistingFactor() throws Exception {
        String admin = adminToken();
        EmissionFactor factor = emissionFactorRepository
                .findFirstByActivityTypeAndUnitAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
                        "TOYS_AND_SPORTS", "USD", LocalDate.now()).orElseThrow();

        mockMvc.perform(put("/api/admin/emission-factors/" + factor.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer(admin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"kgCo2ePerUnit\": 0.290000, \"source\": \"Corrected value\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kgCo2ePerUnit").value(0.29))
                .andExpect(jsonPath("$.source").value("Corrected value"));
    }

    @Test
    void nonAdminCannotManageFactors() throws Exception {
        String user = registerAndGetToken();
        mockMvc.perform(get("/api/admin/emission-factors").header(HttpHeaders.AUTHORIZATION, bearer(user)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/emission-factors").header(HttpHeaders.AUTHORIZATION, bearer(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(34));
    }

    private String adminToken() throws Exception {
        String suffix = uniqueSuffix();
        User admin = new User();
        admin.setUsername("admin_" + suffix);
        admin.setEmail("admin_" + suffix + "@example.com");
        admin.setPasswordHash(passwordEncoder.encode("AdminPass123!"));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        String response = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", admin.getEmail(), "password", "AdminPass123!"))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return readTree(response).get("accessToken").asText();
    }
}
