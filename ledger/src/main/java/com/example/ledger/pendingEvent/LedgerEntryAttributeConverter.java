package com.example.ledger.pendingEvent;

import com.example.ledger.domain.LedgerEntry;
import jakarta.persistence.AttributeConverter;
import org.springframework.web.ErrorResponseException;
import tools.jackson.databind.ObjectMapper;

public class LedgerEntryAttributeConverter implements AttributeConverter<LedgerEntry, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(LedgerEntry payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (ErrorResponseException e) {
            return null;
        }
    }

    @Override
    public LedgerEntry convertToEntityAttribute(String value) {
        try {
            return objectMapper.readValue(value, LedgerEntry.class);
        } catch (ErrorResponseException e) {
            return null;
        }
    }
}
