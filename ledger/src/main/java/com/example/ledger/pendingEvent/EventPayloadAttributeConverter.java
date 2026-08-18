package com.example.ledger.pendingEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.AttributeConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.ErrorResponseException;
import tools.jackson.databind.ObjectMapper;

public class EventPayloadAttributeConverter implements AttributeConverter<EventPayload, String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(EventPayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (ErrorResponseException e) {
            return null;
        }
    }

    @Override
    public EventPayload convertToEntityAttribute(String value) {
        try {
            return objectMapper.readValue(value, EventPayload.class);
        } catch (ErrorResponseException e) {
            return null;
        }
    }
}
