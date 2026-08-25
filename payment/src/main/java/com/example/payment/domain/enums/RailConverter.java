package com.example.payment.domain.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RailConverter implements AttributeConverter<Rail, String> {

    @Override
    public String convertToDatabaseColumn(Rail rail) {
        return rail != null ? rail.name() : null;
    }

    @Override
    public Rail convertToEntityAttribute(String rail) {
        return rail != null ? Rail.valueOf(rail) : null;
    }
}
