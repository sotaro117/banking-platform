package com.example.ledger.domain.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class WalletStatusConverter implements AttributeConverter<WalletStatus, String> {

    @Override
    public String convertToDatabaseColumn(WalletStatus status) {
        return status != null ? status.name() : null;
    }

    @Override
    public WalletStatus convertToEntityAttribute(String status) {
        return status != null ? WalletStatus.valueOf(status) : null;
    }
}
