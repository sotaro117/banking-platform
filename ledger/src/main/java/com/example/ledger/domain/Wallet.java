package com.example.ledger.domain;

import com.example.ledger.domain.enums.AccountType;
import com.example.ledger.domain.enums.WalletStatus;
import jakarta.persistence.*;
import lombok.NonNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "wallet")
public class Wallet {
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(name = "account_type")
    private AccountType accountType;

    @ManyToOne
    @JoinColumn(name = "party_id", referencedColumnName = "id")
    private Party party;

    @Column(length = 3)
    private String currency;

    private WalletStatus status;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    public static Wallet linkAsset(@NonNull UUID id, @NonNull Party party, @NonNull String currency) {
        if (currency.isBlank()) {
            throw new IllegalArgumentException("currency must be set");
        }

        return new Wallet(id, AccountType.ASSET, party, currency, WalletStatus.ACTIVE);
    }

    public static Wallet linkLiability(@NonNull UUID id, @NonNull Party party, @NonNull String currency) {
        if (currency.isBlank()) {
            throw new IllegalArgumentException("currency must be set");
        }

        return new Wallet(id, AccountType.LIABILITY, party, currency, WalletStatus.ACTIVE);
    }

    public static Wallet linkExpense(@NonNull UUID id, @NonNull Party party, @NonNull String currency) {
        if (currency.isBlank()) {
            throw new IllegalArgumentException("currency must be set");
        }

        return new Wallet(id, AccountType.EXPENSE, party, currency, WalletStatus.ACTIVE);
    }

    public static Wallet linkRevenue(@NonNull UUID id, @NonNull Party party, @NonNull String currency) {
        if (currency.isBlank()) {
            throw new IllegalArgumentException("currency must be set");
        }

        return new Wallet(id, AccountType.REVENUE, party, currency, WalletStatus.ACTIVE);
    }

    private Wallet(UUID id, AccountType accountType, Party party, String currency, WalletStatus status) {
        this.id = id;
        this.accountType = accountType;
        this.party = party;
        this.currency = currency;
        this.status = status;
    }

    protected Wallet() {
    }

    public UUID getId() {
        return id;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public Party getPartyId() {
        return party;
    }

    public void setParty(Party party) {
        this.party = party;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public WalletStatus getStatus() {
        return status;
    }

    public void setStatus(WalletStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

}



