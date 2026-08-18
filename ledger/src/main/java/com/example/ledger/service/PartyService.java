package com.example.ledger.service;

import com.example.ledger.domain.Party;
import com.example.ledger.repository.PartyRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PartyService {
    @Autowired
    private PartyRepository partyRepository;

    public Party saveParty(@NonNull Party party) {
//        if (getPartyByName(party.getDisplayName()) != null && getPartyByExternalReference(party.getExternalReference()) != null) {
//            // exception
//            throw new IllegalArgumentException("Party already exists");
//        }

        if (party.getDisplayName().isBlank()) {
            // exception
            throw new IllegalArgumentException("Display name cannot be blank");
        }

        return partyRepository.save(party);
    }

    public Party getPartyByName(String displayName) {
        return partyRepository.findByDisplayName(displayName);
    }

    public Party getPartyByExternalReference(String externalReference) {
        return partyRepository.findByExternalReference(externalReference);
    }
}
