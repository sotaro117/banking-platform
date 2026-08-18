package com.example.ledger.domain.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TransactionStatusConverter implements AttributeConverter<TransactionStatus, String> {

    @Override
    public String convertToDatabaseColumn(TransactionStatus status) {
        return status != null ? status.name() : null;
    }

    @Override
    public TransactionStatus convertToEntityAttribute(String status) {
        return status != null ? TransactionStatus.valueOf(status) : null;
    }
}
