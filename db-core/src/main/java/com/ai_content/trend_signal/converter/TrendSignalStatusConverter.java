package com.ai_content.trend_signal.converter;

import com.ai_content.trend_signal.domain.TrendSignalStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Locale;

@Converter
public class TrendSignalStatusConverter
        implements AttributeConverter<TrendSignalStatus, String> {

    @Override
    public String convertToDatabaseColumn(TrendSignalStatus value) {
        return value == null
                ? null
                : value.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public TrendSignalStatus convertToEntityAttribute(String value) {
        return value == null
                ? null
                : TrendSignalStatus.valueOf(
                        value.toUpperCase(Locale.ROOT)
                );
    }
}