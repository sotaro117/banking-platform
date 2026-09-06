package com.example.payment.domain.swan.onboarding.retrieve;

import java.util.List;

public record AccountHolderOnboardingsResponse(
        List<OnboardingEdge> edges,
        int totalCount
) {
}
