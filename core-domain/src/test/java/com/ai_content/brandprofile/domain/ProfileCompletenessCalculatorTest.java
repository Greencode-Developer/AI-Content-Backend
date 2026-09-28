package com.ai_content.brandprofile.domain;

import com.ai_content.brandprofile.service.ProfileCompletenessCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProfileCompletenessCalculatorTest {
    private final ProfileCompletenessCalculator calculator = new ProfileCompletenessCalculator();

    @ParameterizedTest
    @MethodSource("profileCases")
    void shouldCalculateCompleteness(
            String description,
            String toneOfVoice,
            List<String> colors,
            int expected
    ) {
        int actual = calculator.calculate(
                description, toneOfVoice, colors
        );

        assertEquals(expected, actual);
    }

    static Stream<Arguments> profileCases() {
        return Stream.of(
                Arguments.of(null, null, List.of(), 0),
                Arguments.of(" \t", "\n", List.of(), 0),
                Arguments.of(null, null, List.of("#FFFFFF"), 20),
                Arguments.of("Coffe Brand", null, List.of(), 40),
                Arguments.of(null, "Warm", List.of(), 40),
                Arguments.of(
                        "Coffe Brand", null,
                        List.of("#FFFFFF"), 60
                ),
                Arguments.of(
                        null, "Warm",
                        List.of("#FFFFFF"), 60
                ),
                Arguments.of(
                        "Coffe Brand", "Warm",
                        List.of(), 80
                ),
                Arguments.of(
                        "Coffe Brand", "Warm",
                        List.of("#aabbcc", "#FFFFFF"), 100
                ),
                Arguments.of(null, null, null, 0),
                Arguments.of(null, null, List.of("red", "#XYZ123"), 0)
        );
    }

    @Test
    void shouldRecalculateWhenInformationIsCleared() {
        assertEquals(100, calculator.calculate(
                "Coffe Brand", "Warm", List.of("#FFFFFF")
        ));

        // Xóa mô tả: còn giọng điệu và màu.
        assertEquals(60, calculator.calculate(
                null, "Warm", List.of("#FFFFFF")
        ));

        // Xóa thêm màu: chỉ còn giọng điệu.
        assertEquals(40, calculator.calculate(
                null, "Warm", List.of()
        ));

        // Xóa toàn bộ thông tin tính điểm.
        assertEquals(0, calculator.calculate(
                null, null, List.of()
        ));
    }
}