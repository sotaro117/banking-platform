package com.example.ledger.domain;

import com.example.ledger.domain.enums.AccountType;
import com.example.ledger.domain.enums.PartyType;
import com.example.ledger.domain.enums.WalletStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "wallet")
public class Wallet {
    private static final Set<PartyType> PARTY_ASSET = Set.of(PartyType.COMPANY);
    private static final Set<PartyType> PARTY_LIABILITY = Set.of(PartyType.COMPANY, PartyType.EMPLOYEE, PartyType.VENDOR);
    private static final Set<PartyType> PARTY_EXPENSE_REVENUE = Set.of(PartyType.COMPANY);

    @Getter
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Getter
    @Setter
    @Column(name = "account_type")
    private AccountType accountType;

    @Getter
    @Setter
    @ManyToOne
    @JoinColumn(name = "party_id", referencedColumnName = "id")
    private Party party;

    @Getter
    @OneToMany(mappedBy = "wallet")
    private List<LedgerEntry> entries = new ArrayList<>();

    @Getter
    @Setter
    @Column(length = 3)
    private String currency;

    @Getter
    @Setter
    @Column
    private WalletStatus status;

    @Getter
    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    public static Wallet linkAsset(@NonNull UUID id, @NonNull Party party, @NonNull String currency) {
        validateBlankCurrency(currency);
        validateAvailableAccountType(AccountType.ASSET, party.getPartyType());

        return new Wallet(id, AccountType.ASSET, party, currency, WalletStatus.ACTIVE);
    }

    public static Wallet linkLiability(@NonNull UUID id, @NonNull Party party, @NonNull String currency) {
        validateBlankCurrency(currency);
        validateAvailableAccountType(AccountType.LIABILITY, party.getPartyType());

        return new Wallet(id, AccountType.LIABILITY, party, currency, WalletStatus.ACTIVE);
    }

    public static Wallet linkExpense(@NonNull UUID id, @NonNull Party party, @NonNull String currency) {
        validateBlankCurrency(currency);
        validateAvailableAccountType(AccountType.EXPENSE, party.getPartyType());

        return new Wallet(id, AccountType.EXPENSE, party, currency, WalletStatus.ACTIVE);
    }

    public static Wallet linkRevenue(@NonNull UUID id, @NonNull Party party, @NonNull String currency) {
        validateBlankCurrency(currency);
        validateAvailableAccountType(AccountType.REVENUE, party.getPartyType());

        return new Wallet(id, AccountType.REVENUE, party, currency, WalletStatus.ACTIVE);
    }

    // validation methods
    private static void validateBlankCurrency(String currency) {
        if (currency.isBlank()) {
            throw new IllegalArgumentException("currency must be set");
        }
    }

    private static void validateAvailableAccountType(AccountType accountType, PartyType partyType) {
        switch (accountType) {
            case ASSET:
                if (!PARTY_ASSET.contains(partyType)) throw new IllegalArgumentException("Not eligible for " + partyType + " account");
                break;

            case LIABILITY:
                if (!PARTY_LIABILITY.contains(partyType)) throw new IllegalArgumentException("Not eligible for " + partyType + " account");
                break;

            case EXPENSE, REVENUE:
                if (!PARTY_EXPENSE_REVENUE.contains(partyType)) throw new IllegalArgumentException("Not eligible for " + partyType + " account");
                break;
        }
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
}



