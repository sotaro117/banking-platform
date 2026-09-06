package com.example.payment.domain.swan.onboarding.create.companyInfo.relatedIndividual;

public enum UltimateBeneficialOwnerQualificationType {
    OWNERSHIP("Ownership"), CONTROL("Control"), LEGAL_REPRESENTATIVE("LegalRepresentative");

    public final String label;

    UltimateBeneficialOwnerQualificationType(String label) {
        this.label = label;
    }
}
