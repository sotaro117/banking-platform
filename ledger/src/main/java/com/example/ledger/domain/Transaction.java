package com.example.ledger.domain;

import com.example.ledger.domain.enums.LedgerDirection;
import com.example.ledger.domain.enums.TransactionStatus;
import com.example.ledger.domain.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Temporal;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "transaction")
public class Transaction {
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    private TransactionType type;

    @OneToMany(mappedBy = "transaction")
    private List<LedgerEntry> entries = new ArrayList<>();

    private TransactionStatus status;

    private String description;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    public static Transaction post(TransactionType transactionType, List<LedgerEntry> entries, String description) {
        validateBalance(entries);
        validateCurrency(entries);

        UUID transactionId = UUID.randomUUID();
        Transaction transaction = new Transaction(transactionId, transactionType, TransactionStatus.PENDING, entries, description);
        entries.forEach(e -> e.assignTransaction(transaction));
        return transaction;
    }

    private static void validateBalance(List<LedgerEntry> entries) {
        BigDecimal debit = entries.stream()
                .filter(e -> e.getDirection() == LedgerDirection.DEBIT)
                .map(LedgerEntry::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal credit = entries.stream()
                .filter(e -> e.getDirection() == LedgerDirection.CREDIT)
                .map(LedgerEntry::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        if (debit.compareTo(credit) != 0) {
            throw new IllegalArgumentException("Transaction balance must be equal");
        }
    }

    private static void validateCurrency(List<LedgerEntry> entries) {
        boolean sameCurrency = entries.stream()
                .map(LedgerEntry::getCurrency)
                .distinct()
                .count() == 1;

        if (!sameCurrency) {
            throw new IllegalArgumentException("Currency must be equal");
        }
    }

    private Transaction(UUID id, TransactionType type, TransactionStatus status, List<LedgerEntry> entries, String description) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.entries = entries;
        this.description = description;
    }


    protected Transaction() {
    }

    // getters & setters

    public UUID getId() {
        return id;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<LedgerEntry> getEntries() {
        return entries;
    }
}
