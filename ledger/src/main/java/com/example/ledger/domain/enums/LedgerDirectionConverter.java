package com.example.ledger.domain.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LedgerDirectionConverter implements AttributeConverter<LedgerDirection, String> {

    @Override
    public String convertToDatabaseColumn(LedgerDirection direction) {
        return direction != null ? direction.name() : null;
    }

    @Override
    public LedgerDirection convertToEntityAttribute(String accountName) {
        return accountName != null ? LedgerDirection.valueOf(accountName) : null;
    }
}
