package com.ai_content.brandprofile.domain;

import com.ai_content.brandprofile.service.ProfileCompletenessCalculator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProfileCompletenessCalculatorTest {

    private final ProfileCompletenessCalculator calculator =
            new ProfileCompletenessCalculator();

    @ParameterizedTest
    @CsvSource({
            "0, 0",
            "1, 10",
            "19, 10",
            "20, 20",
            "49, 20",
            "50, 30",
            "99, 30",
            "100, 40",
            "150, 40"
    })
    void shouldCalculateDescriptionScore(int length, int expected) {
        int actual = calculator.calculate(
                "a".repeat(length),
                null,
                List.of()
        );

        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0",
            "1, 10",
            "4, 10",
            "5, 20",
            "9, 20",
            "10, 30",
            "19, 30",
            "20, 40",
            "50, 40"
    })
    void shouldCalculateToneOfVoiceScore(int length, int expected) {
        int actual = calculator.calculate(
                null,
                "a".repeat(length),
                List.of()
        );

        assertEquals(expected, actual);
    }

    @Test
    void shouldReturnZeroForNullAndBlankValues() {
        assertEquals(0, calculator.calculate(null, null, null));
        assertEquals(0, calculator.calculate(" \n ", " \t ", List.of()));
    }

    @Test
    void shouldIgnoreLeadingAndTrailingWhitespace() {
        int actual = calculator.calculate(
                "  " + "a".repeat(19) + "  ",
                "\n" + "b".repeat(4) + "\t",
                List.of()
        );

        assertEquals(20, actual);
    }

    @Test
    void shouldCountUnicodeCodePoints() {
        int actual = calculator.calculate(
                "😀".repeat(19),
                null,
                List.of()
        );

        assertEquals(10, actual);
    }

    @Test
    void shouldAwardTwentyPointsForValidColors() {
        assertEquals(
                20,
                calculator.calculate(null, null, List.of("#aabbcc"))
        );

        assertEquals(
                20,
                calculator.calculate(
                        null,
                        null,
                        List.of("#AABBCC", "#FFFFFF")
                )
        );
    }

    @Test
    void shouldNotAwardPointsWithoutValidColors() {
        int actual = calculator.calculate(
                null,
                null,
                Arrays.asList(null, "", "red", "#XYZXYZ")
        );

        assertEquals(0, actual);
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0, false, 0",
            "20, 5, false, 40",
            "50, 5, false, 50",
            "50, 10, false, 60",
            "50, 10, true, 80",
            "100, 20, true, 100"
    })
    void shouldSumAllScores(
            int descriptionLength,
            int toneLength,
            boolean hasColor,
            int expected
    ) {
        int actual = calculator.calculate(
                "a".repeat(descriptionLength),
                "b".repeat(toneLength),
                hasColor ? List.of("#AABBCC") : List.of()
        );

        assertEquals(expected, actual);
    }

    @Test
    void shouldReduceScoreWhenInformationIsCleared() {
        assertEquals(
                100,
                calculator.calculate(
                        "a".repeat(100),
                        "b".repeat(20),
                        List.of("#AABBCC")
                )
        );

        assertEquals(
                60,
                calculator.calculate(
                        null,
                        "b".repeat(20),
                        List.of("#AABBCC")
                )
        );

        assertEquals(0, calculator.calculate(null, null, List.of()));
    }

    // Check color for whitespace is not awarded points
    @Test
    void shouldNotAwardPointsForColorWithSurroundingWhitespace() {
        int actual = calculator.calculate(
                null,
                null,
                List.of(" #AABBCC ")
        );
        
        assertEquals(0, actual);
    }
}