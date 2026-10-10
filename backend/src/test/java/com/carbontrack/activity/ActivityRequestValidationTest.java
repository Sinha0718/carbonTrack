package com.carbontrack.activity;

import static org.assertj.core.api.Assertions.assertThat;

import com.carbontrack.activity.dto.ElectricityActivityRequest;
import com.carbontrack.activity.dto.FoodActivityRequest;
import com.carbontrack.activity.dto.ShoppingActivityRequest;
import com.carbontrack.activity.dto.TransportActivityRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ActivityRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @ParameterizedTest(name = "distanceKm={0} valid={1}")
    @CsvSource({"0.0001, true", "50000, true", "0, false", "-5, false", "50000.0001, false", "1.12345, false"})
    void transportDistanceBounds(BigDecimal distance, boolean valid) {
        assertThat(violations(new TransportActivityRequest(TransportMode.CAR_PETROL, distance, null, null)).isEmpty())
                .isEqualTo(valid);
    }

    @ParameterizedTest(name = "kwh={0} valid={1}")
    @CsvSource({"0.5, true", "100000, true", "0, false", "-1, false", "100001, false"})
    void electricityKwhBounds(BigDecimal kwh, boolean valid) {
        assertThat(violations(new ElectricityActivityRequest(EnergySource.GRID_AVERAGE, kwh, null, null)).isEmpty())
                .isEqualTo(valid);
    }

    @ParameterizedTest(name = "servings={0} valid={1}")
    @CsvSource({"1, true", "50, true", "0, false", "-1, false", "51, false"})
    void foodServingsBounds(BigDecimal servings, boolean valid) {
        assertThat(violations(new FoodActivityRequest(MealType.BEEF, servings, null, null)).isEmpty())
                .isEqualTo(valid);
    }

    @ParameterizedTest(name = "spend={0} valid={1}")
    @CsvSource({"0.01, true", "1000000, true", "0, false", "-10, false", "1000000.01, false", "9.999, false"})
    void shoppingSpendBounds(BigDecimal spend, boolean valid) {
        assertThat(violations(new ShoppingActivityRequest(ProductCategory.CLOTHING, spend, null, null)).isEmpty())
                .isEqualTo(valid);
    }

    @Test
    void missingActivityTypeAndQuantityAreRejected() {
        assertThat(violations(new TransportActivityRequest(null, null, null, null)))
                .containsExactlyInAnyOrder("mode", "distanceKm");
    }

    @Test
    void futureLogDateIsRejected() {
        var request = new FoodActivityRequest(MealType.VEGAN, BigDecimal.ONE, LocalDate.now().plusDays(1), null);
        assertThat(violations(request)).containsExactly("logDate");
    }

    @Test
    void notesLongerThan500CharsAreRejected() {
        var request = new ShoppingActivityRequest(ProductCategory.CLOTHING, BigDecimal.TEN, null, "x".repeat(501));
        assertThat(violations(request)).containsExactly("notes");
    }

    private <T> Set<String> violations(T request) {
        Set<ConstraintViolation<T>> result = validator.validate(request);
        return result.stream().map(v -> v.getPropertyPath().toString()).collect(Collectors.toSet());
    }
}
