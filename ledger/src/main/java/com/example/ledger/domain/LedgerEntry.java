package com.example.ledger.domain;

import com.example.ledger.domain.enums.LedgerDirection;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "ledger_entry")
public class LedgerEntry {
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(nullable = false)
    @ManyToOne
    @JoinColumn(name = "transaction_id", referencedColumnName = "id")
    private Transaction transaction;

    @Column(nullable = false)
    @ManyToOne
    @JoinColumn(name = "wallet_id", referencedColumnName = "id")
    private Wallet wallet;

    @Column(nullable = false)
    private LedgerDirection direction;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(length = 3, nullable = false)
    private String currency;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    public static LedgerEntry debit(Wallet wallet, BigDecimal amount, String currency) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("amount must be set greater than 0" + currency);
        }

        return new LedgerEntry(null, wallet, LedgerDirection.DEBIT, amount, currency);
    }

    public static LedgerEntry credit(Wallet wallet, BigDecimal amount, String currency) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("amount must be set greater than 0" + currency);
        }

        return new LedgerEntry(null, wallet, LedgerDirection.CREDIT, amount, currency);
    }

    void assignTransaction(Transaction transaction) { this.transaction = transaction; };

    private LedgerEntry(UUID id, Wallet wallet, LedgerDirection direction, BigDecimal amount, String currency) {
        this.id = id;
        this.wallet = wallet;
        this.direction = direction;
        this.amount = amount;
        this.currency = currency;
    }

    protected LedgerEntry() {
    }

    public UUID getId() {
        return id;
    }

    public Transaction getTransactionId() {
        return transaction;
    }

    public void setTransactionId(Transaction transaction) {
        this.transaction = transaction;
    }

    public Wallet getWalletId() {
        return wallet;
    }

    public void setWalletId(Wallet wallet) {
        this.wallet = wallet;
    }

    public LedgerDirection getDirection() {
        return direction;
    }

    public void setDirection(LedgerDirection direction) {
        this.direction = direction;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
