package com.example.ledger.domain.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PartyStatusConverter implements AttributeConverter<PartyStatus, String> {

    @Override
    public String convertToDatabaseColumn(PartyStatus status) {
        return status != null ? status.name() : null;
    }

    @Override
    public PartyStatus convertToEntityAttribute(String status) {
        return status != null ? PartyStatus.valueOf(status) : null;
    }
}
