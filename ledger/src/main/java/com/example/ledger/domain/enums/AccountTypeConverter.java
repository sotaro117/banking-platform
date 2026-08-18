package com.example.ledger.domain.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AccountTypeConverter implements AttributeConverter<AccountType, String> {

    @Override
    public String convertToDatabaseColumn(AccountType accountType) {
        return accountType != null ? accountType.name() : null;
    }

    @Override
    public AccountType convertToEntityAttribute(String accountName) {
        return accountName != null ? AccountType.valueOf(accountName) : null;
    }
}
