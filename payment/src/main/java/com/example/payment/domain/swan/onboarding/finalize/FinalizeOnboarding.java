package com.example.payment.domain.swan.onboarding.finalize;

import com.example.payment.domain.swan.onboarding.create.payload.StatusInfo;

public record FinalizeOnboarding(
        String id,
        FinalizeAccount account,
        StatusInfo statusInfo
) {
}
