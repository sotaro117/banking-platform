package com.example.ledger.domain.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PartyTypeConverter implements AttributeConverter<PartyType, String> {

    @Override
    public String convertToDatabaseColumn(PartyType type) {
        return type != null ? type.name() : null;
    }

    @Override
    public PartyType convertToEntityAttribute(String type) {
        return type != null ? PartyType.valueOf(type) : null;
    }
}
