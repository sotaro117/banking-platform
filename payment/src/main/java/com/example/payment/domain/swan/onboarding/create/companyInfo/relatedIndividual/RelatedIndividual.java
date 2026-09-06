package com.example.payment.domain.swan.onboarding.create.companyInfo.relatedIndividual;

import com.example.payment.domain.swan.onboarding.create.Address;

public record RelatedIndividual(
        RelatedIndividualType type,
        String firstName,
        String lastName,
        Sex sex,
        BirthInfo birthInfo,
        Address address,
        String nationality,
        UltimateBeneficialOwner ultimateBeneficialOwner,
        LegalRepresentative legalRepresentative
        ) {
}