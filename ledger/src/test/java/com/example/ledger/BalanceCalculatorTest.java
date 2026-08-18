package com.example.ledger;

import com.example.ledger.domain.*;
import com.example.ledger.domain.enums.AccountType;
import com.example.ledger.domain.enums.PartyStatus;
import com.example.ledger.domain.enums.PartyType;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class BalanceCalculatorTest {

    // credit - debit == liability
    // or debit - credit == asset
    @Test
    void balanceIsCreditsMinusDebitsForLiablityAccounts() {
        Party company = new Party(UUID.randomUUID(), "external-ref", PartyType.COMPANY, "company", PartyStatus.ACTIVE);
        Wallet wallet = Wallet.linkAsset(UUID.randomUUID(), company, "EUR");

        LedgerEntry entryA = LedgerEntry.debit(wallet, new BigDecimal(1000), "EUR");
        LedgerEntry entryB = LedgerEntry.credit(wallet, new BigDecimal(500), "EUR");

        BigDecimal balance = BalanceCalculator.compute(AccountType.ASSET, List.of(entryA, entryB));

        assertThat(balance).isEqualTo(new BigDecimal(500));
    }

    @Test
    void emptyEntryListMeansZeroBalance() {
        BigDecimal balance = BalanceCalculator.compute(AccountType.ASSET, List.of());

        assertThat(balance).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void onlyEntriesForTheGivenAccountAreSummed() {
        Party company = new Party(UUID.randomUUID(), "external-ref", PartyType.COMPANY, "company", PartyStatus.ACTIVE);
        Party employee = new Party(UUID.randomUUID(), "external-ref", PartyType.EMPLOYEE, "Jane Doe", PartyStatus.ACTIVE);

        Wallet companyWallet = Wallet.linkAsset(UUID.randomUUID(), company, "EUR");
        Wallet employeeWallet = Wallet.linkAsset(UUID.randomUUID(), employee, "EUR");

        LedgerEntry companyEntry = LedgerEntry.debit(companyWallet, new BigDecimal(1000), "EUR");
        LedgerEntry employeeEntry = LedgerEntry.debit(employeeWallet, new BigDecimal(300), "EUR");

        BigDecimal balance = BalanceCalculator.computeForWallet(employeeWallet, AccountType.ASSET, List.of(companyEntry, employeeEntry));

        assertThat(balance).isEqualTo(new BigDecimal(300));
    }
}
