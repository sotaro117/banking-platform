package com.example.payment.domain.swan.onboarding.create.companyInfo;

import com.example.payment.domain.swan.onboarding.create.Address;
import com.example.payment.domain.swan.onboarding.create.companyInfo.relatedIndividual.RelatedIndividual;
import com.example.payment.domain.swan.onboarding.create.companyInfo.relatedIndividual.UnitedStatesTaxInfo;

public record CompanyInfo(
        String companyName,
        BusinessActivityCategory businessActivity,
        String businessActivityDescription,
        String registrationNumber,
        String legalFormCode,
        MonthlyPaymentVolume monthlyPaymentVolume,
        RegulatoryClassification regulatoryClassification,
        Address address,
        RelatedIndividual relatedIndividual
) {
}
