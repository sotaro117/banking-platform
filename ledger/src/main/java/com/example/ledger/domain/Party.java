package com.example.ledger.domain;

import com.example.ledger.domain.enums.AccountType;
import com.example.ledger.domain.enums.PartyStatus;
import com.example.ledger.domain.enums.PartyType;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "party")
public class Party {
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(name = "external_reference", unique = true)
    private String externalReference;

    @Column(nullable = false)
    private PartyType partyType;

    @Column(name = "display_name", length = 50, nullable = false, unique = true)
    private String displayName;

    @Column(nullable = false)
    private PartyStatus status;

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

    public UUID getId() {
        return id;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    public PartyType getPartyType() {
        return partyType;
    }

    public void setPartyType(PartyType partyType) {
        this.partyType = partyType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public PartyStatus getStatus() {
        return status;
    }

    public void setStatus(PartyStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
