package com.example.ledger.service;

import com.example.ledger.domain.Party;
import com.example.ledger.repository.PartyRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.ignoreCase;

@Service
public class PartyService {
    @Autowired
    private PartyRepository partyRepository;

    public Party saveParty(@NonNull Party party) {
        if (party.getDisplayName().isBlank()) {
            // exception
            throw new IllegalArgumentException("Display name cannot be blank");
        }

        return partyRepository.save(party);
    }

    public Party getPartyById(UUID id) {
        Optional<Party> party = partyRepository.findById(id);
        return party.orElse(null);
    }

    public Party getPartyByName(String displayName) {
        return partyRepository.findByDisplayName(displayName);
    }

    public Party getPartyByExternalReference(String externalReference) {
        return partyRepository.findByExternalReference(externalReference);
    }

    public boolean isPartyExist(Party party) {
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("id")
                .withMatcher("display_name", ignoreCase())
                .withMatcher("external_reference", ignoreCase())
                .withMatcher("status", ignoreCase())
                .withMatcher("party_type", ignoreCase());

        Example<Party> example = Example.of(party, matcher);
        return partyRepository.exists(example);
    }
}
