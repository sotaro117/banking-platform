package com.example.payment.domain.swan.onboarding.document;

import java.util.List;

public record GenerateSupportDocumentUrlResponseUpload(
        List<GenerateSupportDocumentUrlResponseField> fields,
        String url
) {
}
