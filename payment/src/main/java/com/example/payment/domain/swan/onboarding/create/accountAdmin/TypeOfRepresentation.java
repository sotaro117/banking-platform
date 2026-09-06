package com.example.payment.domain.swan.onboarding.create.accountAdmin;

public enum TypeOfRepresentation {
    LEGAL_REPRESENTATIVE("LegalRepresentative"), POWER_OF_ATTORNEY("PowerOfAttorney");

    public final String label;

    TypeOfRepresentation(String label) {
        this.label = label;
    }
}
