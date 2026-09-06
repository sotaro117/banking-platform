package com.example.payment.domain.swan.onboarding.create.companyInfo.relatedIndividual;

public enum RelatedIndividualType {
    LegalRepresentative, UltimateBeneficialOwner, LegalRepresentativeAndUltimateBeneficialOwner
}

// The new API uses the company.relatedIndividuals array to represent all individuals connected to the company.
// Each individual has a type that determines their role:
//
//  LegalRepresentative: the individual represents the company legally.
//  UltimateBeneficialOwner: the individual holds ownership or control over the company (25% or more of the company's value, directly or indirectly).
//  LegalRepresentativeAndUltimateBeneficialOwner: the individual serves both roles.