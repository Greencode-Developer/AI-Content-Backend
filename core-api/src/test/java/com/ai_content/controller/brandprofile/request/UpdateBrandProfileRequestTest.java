package com.ai_content.controller.brandprofile.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class UpdateBrandProfileRequestTest {

    private final ObjectMapper mapper = new ObjectMapper()
        .registerModule(
            new ParameterNamesModule(JsonCreator.Mode.DEFAULT)
        );

    @Test
    void shouldAcceptNullTextAndEmptyColors() throws Exception {
        String json = """
                {
                  "description": null,
                  "tone_of_voice": null,
                  "forbidden_words": null,
                  "brand_colors": []
                }
                """;

        UpdateBrandProfileRequest request = mapper.readValue(
                json, UpdateBrandProfileRequest.class
        );

        assertNull(request.description());
        assertNull(request.toneOfVoice());
        assertNull(request.forbiddenWords());
        assertTrue(request.brandColors().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("invalidRequests")
    void shouldRejectInvalidRequest(String json) {
        assertThrows(
                JsonProcessingException.class,
                () -> mapper.readValue(
                        json, UpdateBrandProfileRequest.class
                )
        );
    }

    static Stream<String> invalidRequests() {
        return Stream.of(
                // Thiếu description.
                """
                {
                  "tone_of_voice": null,
                  "forbidden_words": null,
                  "brand_colors": []
                }
                """,

                // Client chỉ định chủ sở hữu.
                """
                {
                  "description": null,
                  "tone_of_voice": null,
                  "forbidden_words": null,
                  "brand_colors": [],
                  "user_id": 999
                }
                """,

                // Client tự đặt điểm.
                """
                {
                  "description": null,
                  "tone_of_voice": null,
                  "forbidden_words": null,
                  "brand_colors": [],
                  "completeness_pct": 100
                }
                """,

                // Sai kiểu văn bản.
                """
                {
                  "description": 123,
                  "tone_of_voice": null,
                  "forbidden_words": null,
                  "brand_colors": []
                }
                """,

                // Danh sách màu không được null.
                """
                {
                  "description": null,
                  "tone_of_voice": null,
                  "forbidden_words": null,
                  "brand_colors": null
                }
                """,

                // Phần tử màu phải là chuỗi.
                """
                {
                  "description": null,
                  "tone_of_voice": null,
                  "forbidden_words": null,
                  "brand_colors": [123]
                }
                """
        );
    }
}