package com.example.payment.domain.swan.onboarding.retrieve;

public record OnboardingInfo(
        String id,
        String createdAt,
        String onboardingUrl,
        OnboardingStatusInfo statusInfo,
        String accountAdmin,
        String name
) {
}
