package com.example.ledger.domain;

import com.example.ledger.domain.enums.PartyStatus;
import com.example.ledger.domain.enums.PartyType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "party")
public class Party {
    @Getter
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Getter
    @Setter
    @Column(name = "external_reference", unique = true)
    private String externalReference;

    @Getter
    @Setter
    @Column(name = "party_type", nullable = false)
    private PartyType partyType;

    @Getter
    @Setter
    @Column(name = "display_name", length = 50, nullable = false, unique = true)
    private String displayName;

    @Getter
    @OneToMany(mappedBy = "party")
    private List<Wallet> wallets = new ArrayList<>();

    @Getter
    @Setter
    @Column(nullable = false)
    private PartyStatus status;

    @Getter
    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;


    public Party(UUID id, String externalReference, PartyType partyType, String displayName, PartyStatus status) {
        this.id = id;
        this.externalReference = externalReference;
        this.partyType = partyType;
        this.displayName = displayName;
        this.status = status;
    }


    protected Party() {
    }
}
