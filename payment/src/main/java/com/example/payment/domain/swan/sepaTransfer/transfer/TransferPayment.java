package com.example.payment.domain.swan.sepaTransfer.transfer;

public record TransferPayment(
        String createdAt,
        String id,
        TransferStatus statusInfo
) {
}
