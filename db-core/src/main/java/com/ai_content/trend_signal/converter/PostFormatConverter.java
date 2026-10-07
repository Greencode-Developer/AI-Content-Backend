package com.ai_content.trend_signal.converter;

import com.ai_content.trend_signal.domain.PostFormat;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Locale;

@Converter
public class PostFormatConverter
        implements AttributeConverter<PostFormat, String> {

    @Override
    public String convertToDatabaseColumn(PostFormat value) {
        return value == null
                ? null
                : value.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public PostFormat convertToEntityAttribute(String value) {
        return value == null
                ? null
                : PostFormat.valueOf(value.toUpperCase(Locale.ROOT));
    }
}