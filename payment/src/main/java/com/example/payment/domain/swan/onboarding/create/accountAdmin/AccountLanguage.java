package com.example.payment.domain.swan.onboarding.create.accountAdmin;

public enum AccountLanguage {
    DE("de"), EN("en"), FR("fr"), IT("it"), NL("nl"), ES("es"), PT("pt"), FI("fi");

    public final String label;

    private AccountLanguage(String label) {
        this.label = label;
    }
}
