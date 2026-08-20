package com.example.ledger.repository;

import com.example.ledger.domain.Party;
import com.example.ledger.domain.enums.PartyType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PartyRepository extends JpaRepository<Party, UUID> {
    Party findByDisplayName(String displayName);

    Party findByExternalReference(String externalReference);
}
