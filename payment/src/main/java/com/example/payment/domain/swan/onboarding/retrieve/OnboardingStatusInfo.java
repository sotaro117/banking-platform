package com.example.payment.domain.swan.onboarding.retrieve;

import java.util.List;

public record OnboardingStatusInfo(
        String __typename,
        List<OnboardingErrors> errors,
        String status
) {
}
