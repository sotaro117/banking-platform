package com.example.payment.domain.swan.onboarding.create.companyInfo;

public enum RegulatoryClassification {
    NON_FINANCIAL_ACTIVE("NonFinancialActive"), NON_FINANCIAL_PASSIVE("NonFinancialPassive"), FINANCIAL_INSTITUTION("FinancialInstitution");

    public final String label;

    RegulatoryClassification(String label) {
        this.label = label;
    }
}
