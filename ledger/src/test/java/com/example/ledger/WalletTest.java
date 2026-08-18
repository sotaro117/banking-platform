package com.example.ledger;

import com.example.ledger.domain.Party;
import com.example.ledger.domain.Wallet;
import com.example.ledger.domain.enums.PartyStatus;
import com.example.ledger.domain.enums.PartyType;
import com.example.ledger.domain.enums.WalletStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class WalletTest {
    @Test
    void assetAccountCanBeLinkedToAParty() {
        Party party = new Party(UUID.randomUUID(), "external-reference", PartyType.COMPANY, "company", PartyStatus.ACTIVE);
        Wallet wallet = Wallet.linkAsset(UUID.randomUUID(), party, "EUR");

        assertThat(wallet.getPartyId()).isEqualTo(party);
    }

    @Test
    void newAccountStartsInActiveStatus() {
        Party party = new Party(UUID.randomUUID(),"external-reference", PartyType.COMPANY, "company", PartyStatus.ACTIVE);
        Wallet wallet = Wallet.linkAsset(UUID.randomUUID(), party, "EUR");

        assertThat(wallet.getStatus()).isEqualTo(WalletStatus.ACTIVE);
    }

    @Test
    void currencyMustBeSet() {
        Party party = new Party(UUID.randomUUID(),"external-reference", PartyType.COMPANY, "company", PartyStatus.ACTIVE);

        assertThatThrownBy(() -> Wallet.linkAsset(UUID.randomUUID(), party, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("currency must be set");
    }
}
