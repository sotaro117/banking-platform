package com.example.ledger.domain;

import com.example.ledger.domain.enums.LedgerDirection;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_entry")
public class LedgerEntry {
    @Getter
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "transaction_id", referencedColumnName = "id", nullable = false)
    private Transaction transaction;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "wallet_id", referencedColumnName = "id", nullable = false)
    private Wallet wallet;

    @Getter
    @Setter
    @Column(nullable = false)
    private LedgerDirection direction;

    @Getter
    @Setter
    @Column(nullable = false)
    private BigDecimal amount;

    @Getter
    @Setter
    @Column(length = 3, nullable = false)
    private String currency;

    @Getter
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
}
