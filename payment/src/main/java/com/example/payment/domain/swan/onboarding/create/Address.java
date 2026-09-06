package com.example.payment.domain.swan.onboarding.create;

public record Address(
        String address,
        String city,
        String country, // cca3 format
        String postalCode
) {
}
