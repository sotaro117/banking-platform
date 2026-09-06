package com.example.payment.domain.swan.onboarding.create.companyInfo.relatedIndividual;

import java.time.LocalDate;

public record BirthInfo(
        LocalDate birthDate,
        String country, // cca3 format
        String city,
        String postalCode
) {
}
