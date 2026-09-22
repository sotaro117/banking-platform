package com.example.payment.domain.swan.consent.retrieve;

public record ConsentResponsePayload(
        String id,
        String challenge,
        String purpose,
        String status
) {
}
