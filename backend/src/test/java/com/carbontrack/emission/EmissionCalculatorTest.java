package com.carbontrack.emission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EmissionCalculatorTest {

    private final EmissionCalculator calculator = new EmissionCalculator();

    @ParameterizedTest(name = "{0}: {1} x {2} = {3} kg CO2e")
    @CsvSource({
            // activityType,      quantity,  kgCo2ePerUnit, expectedKgCo2e
            "CAR_PETROL,          100,       0.170000,      17.0000",
            "FLIGHT_LONG_HAUL,    5500,      0.148000,      814.0000",
            "BICYCLE,             12.5,      0.000000,      0.0000",
            "GRID_AVERAGE,        250,       0.373000,      93.2500",
            "SOLAR,               250,       0.048000,      12.0000",
            "BEEF,                2,         7.500000,      15.0000",
            "VEGAN,               3,         0.300000,      0.9000",
            "ELECTRONICS,         999.99,    0.200000,      199.9980",
            "CLOTHING,            49.95,     0.260000,      12.9870"
    })
    void calculatesCo2eAcrossAllCategories(String activityType, BigDecimal quantity, BigDecimal factor,
                                           BigDecimal expected) {
        assertThat(calculator.calculate(quantity, factor)).isEqualByComparingTo(expected);
    }

    @Test
    void zeroQuantityProducesZeroEmissions() {
        assertThat(calculator.calculate(BigDecimal.ZERO, new BigDecimal("7.5")))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @ParameterizedTest(name = "max {0} x {1} = {2}")
    @CsvSource({
            "50000,      0.246000,  12300.0000",   // max transport distance, highest transport factor
            "100000,     0.820000,  82000.0000",   // max kWh, coal
            "50,         7.500000,  375.0000",     // max servings, beef
            "1000000,    0.380000,  380000.0000",  // max spend, household goods
            "9999999999, 999999.999999, 9999999998990000.0000"  // far beyond API limits: no overflow
    })
    void handlesMaximumValuesWithoutOverflow(BigDecimal quantity, BigDecimal factor, BigDecimal expected) {
        assertThat(calculator.calculate(quantity, factor)).isEqualByComparingTo(expected);
    }

    @Test
    void roundsHalfUpToFourDecimals() {
        BigDecimal result = calculator.calculate(new BigDecimal("0.33333"), new BigDecimal("0.000150"));
        assertThat(result).isEqualByComparingTo("0.0000");
        assertThat(calculator.calculate(new BigDecimal("1"), new BigDecimal("0.000050"))).isEqualByComparingTo("0.0001");
        assertThat(result.scale()).isEqualTo(EmissionCalculator.SCALE);
    }

    @Test
    void rejectsNegativeQuantity() {
        assertThatThrownBy(() -> calculator.calculate(new BigDecimal("-1"), BigDecimal.ONE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNegativeFactor() {
        assertThatThrownBy(() -> calculator.calculate(BigDecimal.ONE, new BigDecimal("-0.1")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNullInputs() {
        assertThatThrownBy(() -> calculator.calculate(null, BigDecimal.ONE)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> calculator.calculate(BigDecimal.ONE, null)).isInstanceOf(NullPointerException.class);
    }
}
