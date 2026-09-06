package com.example.payment.domain.swan.onboarding.retrieve;

import java.util.List;

public record OnboardingErrors(
        String field,
        List<String> errors
) {
}
