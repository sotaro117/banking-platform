package com.example.payment.domain.swan.onboarding.create.companyInfo.relatedIndividual;

public enum UltimateBeneficialOwnerOwnershipType {
    DIRECT("Direct"), INDIRECT("Indirect"), DIRECT_AND_INDRIRECT("DirectAndIndirect");

    public final String label;

    UltimateBeneficialOwnerOwnershipType(String label) {
        this.label = label;
    }
}
