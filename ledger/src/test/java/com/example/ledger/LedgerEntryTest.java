package com.example.ledger;

import com.example.ledger.domain.LedgerEntry;
import com.example.ledger.domain.Party;
import com.example.ledger.domain.Wallet;
import com.example.ledger.domain.enums.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class LedgerEntryTest {
    @Test
    void amountMustBePositive() {
        Party party = new Party(UUID.randomUUID(), "externalAccount", PartyType.COMPANY, "company", PartyStatus.ACTIVE);

        Wallet wallet = Wallet.linkAsset(UUID.randomUUID(), party, "EUR");

        assertThatThrownBy(() -> LedgerEntry.debit(wallet, new BigDecimal(-10), "EUR"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("amount must be set greater than 0EUR");
    }

    @Test
    void directionMustBeDebitOrCredit() {
        Party party = new Party(UUID.randomUUID(),"externalAccount", PartyType.COMPANY, "company", PartyStatus.ACTIVE);
        Wallet wallet = Wallet.linkAsset(UUID.randomUUID(), party, "EUR");

        LedgerEntry ledgerEntry = LedgerEntry.debit(wallet, new BigDecimal(0), "EUR" );

        assertThat(ledgerEntry.getDirection()).isIn(LedgerDirection.values());
    }
}
