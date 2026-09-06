package com.example.payment.domain.swan.onboarding.create.companyInfo;

public enum MonthlyPaymentVolume {
    LESS_THAN_10000("LessThan10000"),
    BETWEEN_10000_AND_50000("Between10000And50000"),
    BETWEEN_50000_AND_100000("Between50000And100000"),
    MORE_THAN_100000("MoreThan100000");

    public final String label;

    private MonthlyPaymentVolume(String label) {
        this.label = label;
    }
}
