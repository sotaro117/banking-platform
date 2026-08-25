package com.example.payment.domain.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PaymentStateConverter implements AttributeConverter<PaymentState, String> {

    @Override
    public String convertToDatabaseColumn(PaymentState state) {
        return state != null ? state.name() : null;
    }

    @Override
    public PaymentState convertToEntityAttribute(String state) {
        return state != null ? PaymentState.valueOf(state) : null;
    }
}
