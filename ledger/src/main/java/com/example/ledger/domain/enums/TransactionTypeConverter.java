package com.example.ledger.domain.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TransactionTypeConverter implements AttributeConverter<TransactionType, String> {

    @Override
    public String convertToDatabaseColumn(TransactionType type) {
        return type != null ? type.name() : null;
    }

    @Override
    public TransactionType convertToEntityAttribute(String type) {
        return type != null ? TransactionType.valueOf(type) : null;
    }
}
