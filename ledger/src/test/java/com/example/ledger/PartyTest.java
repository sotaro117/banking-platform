package com.example.ledger;

import com.example.ledger.domain.Party;
import com.example.ledger.domain.enums.PartyStatus;
import com.example.ledger.domain.enums.PartyType;
import com.example.ledger.repository.PartyRepository;
import com.example.ledger.service.PartyService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class PartyTest {
    @Mock
    private PartyRepository partyRepository;

    @InjectMocks
    private PartyService partySerivce; // injects the mock into the service being tested

    private static final String displayName = "Jane Doe";
    private static final String externalAccount = "example_account";
    private static final String id = "1";

    @Test
    void openPartyAccountForEmployee() {

        Party party = new Party(UUID.fromString(id), externalAccount, PartyType.EMPLOYEE, displayName, PartyStatus.ACTIVE);
        given(partySerivce.saveParty(party)).willReturn(party);

        Party createdParty = partySerivce.saveParty(party);

        assertThat(createdParty.getId()).isEqualTo(UUID.fromString(id));
        assertThat(createdParty.getDisplayName()).isEqualTo(displayName);
        assertThat(createdParty.getPartyType()).isEqualTo(PartyType.EMPLOYEE);
        assertThat(createdParty.getStatus()).isEqualTo(PartyStatus.ACTIVE);
    }

//    @Test
//    void newPartyStartsActive() throws IllegalArgumentException {
//        Party party = new Party(externalAccount, PartyType.EMPLOYEE, displayName, null);
//
//        assertThatThrownBy(() -> partySerivce.saveParty(party))
//                .isInstanceOf(IllegalArgumentException.class);
//
//        then(partyRepository)
//                .should(never())
//                .save(party);
//    }

    @Test
    void displayNameCannotBeBlank() {
        Party party = new Party(UUID.fromString(id), externalAccount, PartyType.EMPLOYEE, "", PartyStatus.ACTIVE);

        assertThatThrownBy(() -> partySerivce.saveParty(party))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Display name cannot be blank");

        then(partyRepository)
                .should((never()))
                .save(party);
    }
}
