package com.example.payment.domain.swan.onboarding.create.accountAdmin;

public record AccountAdmin(
        String email,
        AccountLanguage preferredLanguage,
        TypeOfRepresentation typeOfRepresentation // check
) {
}
