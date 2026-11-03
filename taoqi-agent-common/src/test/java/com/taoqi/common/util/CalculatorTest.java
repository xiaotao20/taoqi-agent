package com.taoqi.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CalculatorTest {

    @Test
    @DisplayName("加法：1 + 2 = 3")
    void add() {
        assertThat(Calculator.add(1, 2)).isEqualTo(3);
    }

    @ParameterizedTest(name = "{0} / {1} = {2}")
    @CsvSource({"10, 2, 5", "-9, 3, -3", "7, 7, 1"})
    void divide(int dividend, int divisor, int expected) {
        assertThat(Calculator.divide(dividend, divisor)).isEqualTo(expected);
    }

    @Test
    @DisplayName("除数为 0 时抛出 IllegalArgumentException")
    void divideByZeroThrows() {
        assertThatThrownBy(() -> Calculator.divide(1, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("除数不能为 0");
    }
}
