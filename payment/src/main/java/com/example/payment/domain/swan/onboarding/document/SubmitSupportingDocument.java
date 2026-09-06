package com.example.payment.domain.swan.onboarding.document;

import java.util.UUID;

public record SubmitSupportingDocument(
        String supportingDocumentCollectionId,
        String filename,
        SupportingDocumentPurpose supportingDocumentPurpose,
        SupportingDocumentType supportingDocumentType
) {
}
