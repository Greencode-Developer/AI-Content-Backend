package com.ai_content.brandprofile.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProfileCompletenessCalculatorTest {

    @ParameterizedTest
    @MethodSource("profileCases")
    void shouldCalculateCompleteness(
            String description,
            String toneOfVoice,
            List<String> colors,
            int expected
    ) {
        int actual = ProfileCompletenessCalculator.calculate(
                description, toneOfVoice, colors
        );

        assertEquals(expected, actual);
    }

    static Stream<Arguments> profileCases() {
        return Stream.of(
                Arguments.of(null, null, List.of(), 0),
                Arguments.of(" \t", "\n", List.of(), 0),
                Arguments.of(null, null, List.of("#FFFFFF"), 20),
                Arguments.of("Thương hiệu cà phê", null, List.of(), 40),
                Arguments.of(null, "Ấm áp", List.of(), 40),
                Arguments.of(
                        "Thương hiệu cà phê", null,
                        List.of("#FFFFFF"), 60
                ),
                Arguments.of(
                        null, "Ấm áp",
                        List.of("#FFFFFF"), 60
                ),
                Arguments.of(
                        "Thương hiệu cà phê", "Ấm áp",
                        List.of(), 80
                ),
                Arguments.of(
                        "Thương hiệu cà phê", "Ấm áp",
                        List.of("#aabbcc", "#FFFFFF"), 100
                ),
                Arguments.of(null, null, null, 0),
                Arguments.of(null, null, List.of("red", "#XYZ123"), 0)
        );
    }

    @Test
    void shouldRecalculateWhenInformationIsCleared() {
        assertEquals(100, ProfileCompletenessCalculator.calculate(
                "Thương hiệu cà phê", "Ấm áp", List.of("#FFFFFF")
        ));

        // Xóa mô tả: còn giọng điệu và màu.
        assertEquals(60, ProfileCompletenessCalculator.calculate(
                null, "Ấm áp", List.of("#FFFFFF")
        ));

        // Xóa thêm màu: chỉ còn giọng điệu.
        assertEquals(40, ProfileCompletenessCalculator.calculate(
                null, "Ấm áp", List.of()
        ));

        // Xóa toàn bộ thông tin tính điểm.
        assertEquals(0, ProfileCompletenessCalculator.calculate(
                null, null, List.of()
        ));
    }
}