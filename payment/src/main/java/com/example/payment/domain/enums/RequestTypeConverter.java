package com.example.payment.domain.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RequestTypeConverter implements AttributeConverter<RequestType, String> {

    @Override
    public String convertToDatabaseColumn(RequestType type) {
        return type != null ? type.name() : null;
    }

    @Override
    public RequestType convertToEntityAttribute(String type) {
        return type != null ? RequestType.valueOf(type) : null;
    }
}
