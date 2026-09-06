package com.example.payment.domain.swan.onboarding.create.payload;

public record CompanyOnboardingResponse(
        String id,
        StatusInfo statusInfo,
        SupportingDocumentCollection supportingDocumentCollection
) {
}
