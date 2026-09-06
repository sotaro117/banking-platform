package com.example.payment.domain.swan.onboarding.create.companyInfo.relatedIndividual;

public enum Sex {
    MALE("Male"), FEMALE("Female"), UNKNOWN("Unkwon");

    public final String label;

    Sex(String label) {
        this.label = label;
    }
}
