package com.example.payment.connector;

import com.example.payment.domain.PaymentRequest;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

// request to swan api
// credit transfer
@Data
public class PaymentInstruction {
    private String idempotencyKey;
    private String accountId;
    private BigDecimal amount;
    private String currency;
    private String name;
    private String iban;

    private PaymentInstruction(String idempotencyKey, String accountId, BigDecimal amount, String currency, String name, String iban) {
        this.idempotencyKey = idempotencyKey;
        this.accountId = accountId;
        this.amount = amount;
        this.currency = currency;
        this.name = name;
        this.iban = iban;
    }

    public static PaymentInstruction from(PaymentRequest request) {
        return new PaymentInstruction(request.getIdempotencyKey(), request.getDebitAccount().getSwanAccountId(), request.getAmount(), request.getCurrency(), "jane doe", request.getCreditAccount().getIban());
    }
}
