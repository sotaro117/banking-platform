package com.example.payment.domain;

import com.example.payment.domain.enums.Rail;
import jakarta.persistence.*;
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
    @Column(name = "wallet_reference")
    private UUID walletReference;

    @Getter @Setter
    private Rail rail;

    @Getter @Setter
    @Column(name = "stripe_financial_account_id")
    private String stripeFinancialAccountId;

    @Getter @Setter
    private String label;

    @Getter
    @CreationTimestamp
    private Instant createdAt;


    public ExternalAccount(UUID id, UUID walletReference, Rail rail, String stripeFinancialAccountId, String label) {
        this.id = id;
        this.walletReference = walletReference;
        this.rail = rail;
        this.stripeFinancialAccountId = stripeFinancialAccountId;
        this.label = label;
    }

    protected ExternalAccount() {
    }
}
