package com.example.payment.domain;

import com.example.payment.domain.enums.Rail;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "external_account")
public class ExternalAccount {
    @Getter
    @Id
    private UUID id;

    @Getter @Setter
    @Column(name = "holder_name", nullable = false)
    private String holderName;

    @Getter @Setter
    @Column(name = "wallet_reference")
    private UUID walletReference;

    @Getter @Setter
    private Rail rail;

    @Getter @Setter
    @Column(name = "swan_account_id´")
    private String swanAccountId;

    @Getter @Setter
    @Column(name = "swan_iban")
    private String swanIban;

    @Getter @Setter
    private String iban;

    @Getter @Setter
    private String label;

    @Getter
    @CreationTimestamp
    private Instant createdAt;

    protected ExternalAccount() {
    }

    private ExternalAccount(UUID id, String holderName, UUID walletReference, Rail rail, String swanAccountId, String swanIban, String iban, String label, Instant createdAt) {
        this.id = id;
        this.holderName = holderName;
        this.walletReference = walletReference;
        this.rail = rail;
        this.swanAccountId = swanAccountId;
        this.swanIban = swanIban;
        this.iban = iban;
        this.label = label;
        this.createdAt = createdAt;
    }

    public static ExternalAccount companyAccount(String holderName, UUID walletReference, Rail rail, String swanAccountId, String swanIban, String label) {
        return new ExternalAccount(UUID.randomUUID(), holderName, walletReference, rail, swanAccountId, swanIban, null, label, Instant.now());
    }

    public static ExternalAccount individualAccount(String holderName, UUID walletReference, Rail rail, String iban, String label) {
        return new ExternalAccount(UUID.randomUUID(), holderName, walletReference, rail, null, null, iban, label, Instant.now());
    }
}
