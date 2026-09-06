package com.example.payment.domain;

import com.example.payment.domain.enums.PaymentState;
import com.example.payment.domain.enums.Rail;
import com.example.payment.domain.enums.RequestType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_request")
public class PaymentRequest {
    @Getter
    @Id
    private UUID id;

    @Getter @Setter
    @Column(name = "ledger_transaction_id")
    private UUID ledgerTransactionId;

    @Getter @Setter
    private Rail rail;

    @Getter @Setter
    @ManyToOne
    @JoinColumn(name = "credit_account_id", referencedColumnName = "id")
    private ExternalAccount creditAccount;

    @Setter @Getter
    @ManyToOne
    @JoinColumn(name = "debit_account_id", referencedColumnName = "id")
    private ExternalAccount debitAccount;

    @Getter @Setter
    @Column(name = "request_type")
    private RequestType requestType;

    @Getter @Setter
    private PaymentState paymentState;

    @Getter @Setter
    @Column(name = "idempotency_key")
    private String idempotencyKey;

    @Getter @Setter
    private BigDecimal amount;

    @Getter @Setter
    @Column(length = 3)
    private String currency;

    @Getter
    @Column(name = "stripe_reference")
    private String stripeReference;

    @Setter @Getter
    @Column(name = "failure_reason")
    private String failureReason;

    @Getter
    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    protected PaymentRequest() {
    }

    private PaymentRequest(UUID id, UUID ledgerTransactionId, Rail rail, ExternalAccount creditAccount, ExternalAccount debitAccount, RequestType requestType, PaymentState paymentState, String idempotencyKey, BigDecimal amount, String currency, String stripeReference, String failureReason, Instant createdAt) {
        this.id = id;
        this.ledgerTransactionId = ledgerTransactionId;
        this.rail = rail;
        this.creditAccount = creditAccount;
        this.debitAccount = debitAccount;
        this.requestType = requestType;
        this.paymentState = paymentState;
        this.idempotencyKey = idempotencyKey;
        this.amount = amount;
        this.currency = currency;
        this.stripeReference = stripeReference;
        this.failureReason = failureReason;
        this.createdAt = createdAt;
    }

    public static PaymentRequest create(UUID id, ExternalAccount debitAccount, ExternalAccount creditAccount, RequestType type,BigDecimal amount, String currency) {
        return new PaymentRequest(id, null, debitAccount.getRail(), creditAccount, debitAccount, type, PaymentState.INITIATED, null, amount, currency, null, null, Instant.now());
    }
}
