package com.example.ledger.domain;

import com.example.ledger.domain.enums.Rail;
import com.example.ledger.domain.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@Data
public class PostTransactionRequest {
    private UUID id;

    private UUID ledgerTransactionId;

    private Rail rail;

    private UUID creditAccount;

    private String externalAccount;

    private TransactionType requestType;

    private String paymentState;

    private String idempotencyKey;

    private BigDecimal amount;

    private String currency;

    private String stripeReference;

    private String failureReason;

    private Instant createdAt;
}