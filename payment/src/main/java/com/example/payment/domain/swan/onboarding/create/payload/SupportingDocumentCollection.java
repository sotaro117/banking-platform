package com.example.payment.domain.swan.onboarding.create.payload;

public record SupportingDocumentCollection(
        RequiredSupportingDocumentPurposes requiredSupportingDocumentPurposes,
        StatusInfo statusInfo
) {
}
