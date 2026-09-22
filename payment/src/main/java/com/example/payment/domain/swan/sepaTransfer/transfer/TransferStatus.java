package com.example.payment.domain.swan.sepaTransfer.transfer;

public record TransferStatus(
        String status,
        String __typename,
        TransferConsent consent
) {
}
