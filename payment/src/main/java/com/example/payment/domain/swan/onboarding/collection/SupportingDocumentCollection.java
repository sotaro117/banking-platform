package com.example.payment.domain.swan.onboarding.collection;

import com.example.payment.domain.swan.onboarding.create.payload.StatusInfo;

public record SupportingDocumentCollection(
        String id,
        StatusInfo statusInfo
) {
}
