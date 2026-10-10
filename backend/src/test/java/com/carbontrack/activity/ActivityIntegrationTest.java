package com.carbontrack.activity;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.carbontrack.support.AbstractIntegrationTest;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

class ActivityIntegrationTest extends AbstractIntegrationTest {

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        token = registerAndGetToken();
    }

    @ParameterizedTest(name = "{0} {1} -> {4} kg CO2e")
    @CsvSource(delimiter = '|', value = {
            "transport   | {\"mode\":\"CAR_PETROL\",\"distanceKm\":100}                | TRANSPORT   | km      | 17.0",
            "transport   | {\"mode\":\"FLIGHT_LONG_HAUL\",\"distanceKm\":5500}         | TRANSPORT   | km      | 814.0",
            "electricity | {\"energySource\":\"GRID_AVERAGE\",\"kwh\":250}             | ELECTRICITY | kWh     | 93.25",
            "food        | {\"mealType\":\"BEEF\",\"servings\":2}                      | FOOD        | serving | 15.0",
            "shopping    | {\"productCategory\":\"ELECTRONICS\",\"spendAmount\":999.99} | SHOPPING    | USD     | 199.998"
    })
    void logsActivityAndPersistsCalculatedCo2e(String path, String body, String category, String unit,
                                               double expectedCo2e) throws Exception {
        String response = postActivity(path, body)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category").value(category))
                .andExpect(jsonPath("$.unit").value(unit))
                .andExpect(jsonPath("$.co2eKg").value(expectedCo2e))
                .andExpect(jsonPath("$.emissionFactorId").isNumber())
                .andExpect(jsonPath("$.logDate").value(LocalDate.now().toString()))
                .andReturn().getResponse().getContentAsString();

        long id = readTree(response).get("id").asLong();
        mockMvc.perform(get("/api/activities/" + id).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.co2eKg").value(expectedCo2e));
    }

    @ParameterizedTest(name = "{0}: {1}")
    @CsvSource(delimiter = '|', value = {
            "transport   | {\"mode\":\"CAR_PETROL\",\"distanceKm\":0}",
            "transport   | {\"mode\":\"CAR_PETROL\",\"distanceKm\":-10}",
            "transport   | {\"mode\":\"CAR_PETROL\",\"distanceKm\":60000}",
            "transport   | {\"distanceKm\":10}",
            "transport   | {\"mode\":\"TELEPORT\",\"distanceKm\":10}",
            "electricity | {\"energySource\":\"SOLAR\"}",
            "food        | {\"mealType\":\"BEEF\",\"servings\":51}",
            "shopping    | {\"productCategory\":\"CLOTHING\",\"spendAmount\":10,\"logDate\":\"2999-01-01\"}",
            "shopping    | not-json"
    })
    void rejectsMalformedOrOutOfRangePayloads(String path, String body) throws Exception {
        postActivity(path, body).andExpect(status().isBadRequest());
    }

    @Test
    void listsActivitiesFilteredByCategoryAndDateRange() throws Exception {
        postActivity("transport", "{\"mode\":\"BUS\",\"distanceKm\":10,\"logDate\":\"2025-03-01\"}")
                .andExpect(status().isCreated());
        postActivity("food", "{\"mealType\":\"VEGAN\",\"servings\":1,\"logDate\":\"2025-03-02\"}")
                .andExpect(status().isCreated());
        postActivity("food", "{\"mealType\":\"LAMB\",\"servings\":1,\"logDate\":\"2025-04-15\"}")
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/activities").header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content[0].activityType").value("LAMB"));

        mockMvc.perform(get("/api/activities").param("category", "FOOD")
                        .param("from", "2025-03-01").param("to", "2025-03-31")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].activityType").value("VEGAN"))
                .andExpect(jsonPath("$.content[0].co2eKg").value(0.3));
    }

    @Test
    void usersCannotSeeOrDeleteOthersActivities() throws Exception {
        String response = postActivity("electricity", "{\"energySource\":\"COAL\",\"kwh\":10}")
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id = readTree(response).get("id").asLong();

        String otherToken = registerAndGetToken();
        mockMvc.perform(get("/api/activities/" + id).header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/activities/" + id).header(HttpHeaders.AUTHORIZATION, bearer(otherToken)))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/activities/" + id).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/activities/" + id).header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void loggingRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/activities/food").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mealType\":\"BEEF\",\"servings\":1}"))
                .andExpect(status().isUnauthorized());
    }

    private ResultActions postActivity(String path, String body) throws Exception {
        return mockMvc.perform(post("/api/activities/" + path)
                .header(HttpHeaders.AUTHORIZATION, bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}
