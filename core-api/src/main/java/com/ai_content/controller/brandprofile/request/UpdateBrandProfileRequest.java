package com.ai_content.controller.brandprofile.request;

import com.ai_content.brandprofile.command.UpdateBrandProfileCommand;
import com.ai_content.common.error.CustomException;
import com.ai_content.common.error.ErrorCode;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public record UpdateBrandProfileRequest(
        @JsonProperty("description") String description,
        @JsonProperty("tone_of_voice") String toneOfVoice,
        @JsonProperty("forbidden_words") String forbiddenWords,
        @JsonProperty("brand_colors") List<String> brandColors
) {
    @JsonCreator(mode = JsonCreator.Mode.DISABLED)
    public UpdateBrandProfileRequest {
        
    }

    private static final Set<String> FIELDS = Set.of(
            "description",
            "tone_of_voice",
            "forbidden_words",
            "brand_colors"
    );

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static UpdateBrandProfileRequest fromJson(JsonNode body) {
        if (body == null || !body.isObject()) {
            throw invalid("Request phải là một JSON object");
        }

        for (String field : FIELDS) {
            if (!body.has(field)) {
                throw invalid("Thiếu trường: " + field);
            }
        }

        var names = body.fieldNames();
        while (names.hasNext()) {
            String name = names.next();

            if (!FIELDS.contains(name)) {
                throw invalid("Trường không được phép: " + name);
            }
        }

        JsonNode colorsNode = body.get("brand_colors");

        if (!colorsNode.isArray()) {
            throw invalid("brand_colors phải là mảng; dùng [] để xóa màu");
        }

        List<String> colors = new ArrayList<>();

        for (JsonNode color : colorsNode) {
            if (!color.isTextual()) {
                throw invalid("Mỗi phần tử brand_colors phải là chuỗi");
            }

            colors.add(color.textValue());
        }

        return new UpdateBrandProfileRequest(
                nullableText(body.get("description"), "description"),
                nullableText(body.get("tone_of_voice"), "tone_of_voice"),
                nullableText(body.get("forbidden_words"), "forbidden_words"),
                List.copyOf(colors)
        );
    }

    public UpdateBrandProfileCommand toCommand() {
        return new UpdateBrandProfileCommand(
                description,
                toneOfVoice,
                forbiddenWords,
                brandColors
        );
    }

    private static String nullableText(JsonNode value, String field) {
        if (value.isNull()) {
            return null;
        }

        if (!value.isTextual()) {
            throw invalid(field + " phải là chuỗi hoặc null");
        }

        return value.textValue();
    }

    private static CustomException invalid(String message) {
        return new CustomException(
                ErrorCode.BRAND_PROFILE_VALIDATION_ERROR,
                message
        );
    }
}