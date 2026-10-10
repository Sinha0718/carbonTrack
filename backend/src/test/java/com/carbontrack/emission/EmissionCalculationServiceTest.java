package com.carbontrack.emission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.carbontrack.activity.ActivityCategory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmissionCalculationServiceTest {

    private static final LocalDate DATE = LocalDate.of(2025, 6, 1);

    @Mock
    private EmissionFactorRepository repository;

    private EmissionCalculationService service;

    @BeforeEach
    void setUp() {
        service = new EmissionCalculationService(repository, new EmissionCalculator());
    }

    @ParameterizedTest(name = "{0} {1}: {3} {2} -> {4} kg")
    @CsvSource({
            "TRANSPORT,   CAR_DIESEL,   km,      42.5,  0.171000, 7.2675",
            "ELECTRICITY, NATURAL_GAS,  kWh,     310,   0.490000, 151.9000",
            "FOOD,        CHICKEN,      serving, 1.5,   0.750000, 1.1250",
            "SHOPPING,    FURNITURE,    USD,     420,   0.320000, 134.4000"
    })
    void looksUpFactorAndCalculatesForEachCategory(ActivityCategory category, String type, String unit,
                                                    BigDecimal quantity, BigDecimal factorValue, BigDecimal expected) {
        EmissionFactor factor = factor(category, type, unit, factorValue);
        when(repository.findFirstByActivityTypeAndUnitAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
                type, unit, DATE)).thenReturn(Optional.of(factor));

        EmissionResult result = service.calculate(type, unit, quantity, DATE);

        assertThat(result.co2eKg()).isEqualByComparingTo(expected);
        assertThat(result.factor()).isSameAs(factor);
    }

    @Test
    void zeroQuantityReturnsZero() {
        when(repository.findFirstByActivityTypeAndUnitAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
                "BEEF", "serving", DATE))
                .thenReturn(Optional.of(factor(ActivityCategory.FOOD, "BEEF", "serving", new BigDecimal("7.5"))));

        assertThat(service.calculate("BEEF", "serving", BigDecimal.ZERO, DATE).co2eKg())
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @ParameterizedTest
    @ValueSource(strings = {"HOVERBOARD", "car_petrol", "UNKNOWN_TYPE"})
    void unknownActivityTypeThrows(String type) {
        when(repository.findFirstByActivityTypeAndUnitAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
                anyString(), anyString(), any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.calculate(type, "km", BigDecimal.TEN, DATE))
                .isInstanceOf(UnknownEmissionFactorException.class)
                .hasMessageContaining(type);
    }

    @Test
    void knownTypeWithWrongUnitThrows() {
        when(repository.findFirstByActivityTypeAndUnitAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(
                "CAR_PETROL", "miles", DATE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.calculate("CAR_PETROL", "miles", BigDecimal.TEN, DATE))
                .isInstanceOf(UnknownEmissionFactorException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void blankActivityTypeThrowsWithoutQueryingDatabase(String type) {
        assertThatThrownBy(() -> service.calculate(type, "km", BigDecimal.TEN, DATE))
                .isInstanceOf(UnknownEmissionFactorException.class);
        verifyNoInteractions(repository);
    }

    private static EmissionFactor factor(ActivityCategory category, String type, String unit, BigDecimal value) {
        EmissionFactor factor = new EmissionFactor();
        factor.setId(1L);
        factor.setCategory(category);
        factor.setActivityType(type);
        factor.setUnit(unit);
        factor.setKgCo2ePerUnit(value);
        factor.setSource("test");
        factor.setEffectiveDate(LocalDate.of(2000, 1, 1));
        return factor;
    }
}
