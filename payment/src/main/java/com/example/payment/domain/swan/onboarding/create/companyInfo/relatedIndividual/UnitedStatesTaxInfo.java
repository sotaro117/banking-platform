package com.example.payment.domain.swan.onboarding.create.companyInfo.relatedIndividual;

public record UnitedStatesTaxInfo(
        boolean isUnitedStatesPerson,
        String unitedStatesTaxIdentificationNumber
) {
}
