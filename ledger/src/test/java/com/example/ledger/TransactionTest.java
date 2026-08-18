package com.example.ledger;

import com.example.ledger.domain.LedgerEntry;
import com.example.ledger.domain.Party;
import com.example.ledger.domain.Transaction;
import com.example.ledger.domain.Wallet;
import com.example.ledger.domain.enums.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

class TransactionTest {
    // check net to zero mechanism
    @Test
    void balancedEntriesAreAccepted() {
        Party companyParty = new Party(UUID.randomUUID(), "external-reference", PartyType.COMPANY, "company", PartyStatus.ACTIVE);
        Party employeeParty = new Party(UUID.randomUUID(), "external-reference", PartyType.EMPLOYEE, "Jane Doe", PartyStatus.ACTIVE);

        Wallet companyWallet = Wallet.linkExpense(UUID.randomUUID(), companyParty, "EUR");
        Wallet employeeWallet = Wallet.linkLiability(UUID.randomUUID(), employeeParty, "EUR");

        LedgerEntry companyEntry = LedgerEntry.debit(companyWallet, new BigDecimal(100), "EUR" );
        LedgerEntry employeeEntry = LedgerEntry.credit(employeeWallet, new BigDecimal(100), "EUR");

        Transaction tx = Transaction.post(TransactionType.PAYROLL, List.of(companyEntry, employeeEntry), "payroll");

        assertThat(tx.getEntries().get(0).getAmount()).isEqualTo(tx.getEntries().get(1).getAmount());
    }

    @Test
    void unbalancedEntriesAreRejected() {
        Party companyParty = new Party(UUID.randomUUID(), "external-reference", PartyType.COMPANY, "company", PartyStatus.ACTIVE);
        Party employeeParty = new Party(UUID.randomUUID(), "external-reference", PartyType.EMPLOYEE, "Jane Doe", PartyStatus.ACTIVE);

        Wallet companyWallet = Wallet.linkExpense(UUID.randomUUID(), companyParty, "EUR");
        Wallet employeeWallet = Wallet.linkLiability(UUID.randomUUID(), employeeParty, "EUR");

        LedgerEntry companyEntry = LedgerEntry.debit(companyWallet, new BigDecimal(50), "EUR" );
        LedgerEntry employeeEntry = LedgerEntry.credit(employeeWallet, new BigDecimal(100), "EUR");

        assertThatThrownBy(() -> Transaction.post(TransactionType.PAYROLL, List.of(companyEntry, employeeEntry), "payroll"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Transaction balance must be equal");
    }

    @Test
    void transactionNeedsAtLeastTwoEntries() {
        Party companyParty = new Party(UUID.randomUUID(), "external-reference", PartyType.COMPANY, "company", PartyStatus.ACTIVE);
        Party employeeParty = new Party(UUID.randomUUID(), "external-reference", PartyType.EMPLOYEE, "Jane Doe", PartyStatus.ACTIVE);

        Wallet companyWallet = Wallet.linkExpense(UUID.randomUUID(), companyParty, "EUR");
        Wallet employeeWallet = Wallet.linkLiability(UUID.randomUUID(), employeeParty, "EUR");

        LedgerEntry companyEntry = LedgerEntry.debit(companyWallet, new BigDecimal(100), "EUR" );
        LedgerEntry employeeEntry = LedgerEntry.credit(employeeWallet, new BigDecimal(100), "EUR");

        Transaction transaction = Transaction.post(TransactionType.PAYROLL, List.of(companyEntry, employeeEntry), "payroll");

        assertThat(transaction.getEntries().stream().count()).isEqualTo(2);
    }

    @Test
    void entriesInDifferentCurrenciesAreRejected() {
        Party companyParty = new Party(UUID.randomUUID(), "external-reference", PartyType.COMPANY, "company", PartyStatus.ACTIVE);
        Party employeeParty = new Party(UUID.randomUUID(), "external-reference", PartyType.EMPLOYEE, "Jane Doe", PartyStatus.ACTIVE);

        Wallet companyWallet = Wallet.linkExpense(UUID.randomUUID(), companyParty, "EUR");
        Wallet employeeWallet = Wallet.linkLiability(UUID.randomUUID(), employeeParty, "EUR");

        LedgerEntry companyEntry = LedgerEntry.debit(companyWallet, new BigDecimal(100), "EUR" );
        LedgerEntry employeeEntry = LedgerEntry.credit(employeeWallet, new BigDecimal(100), "USD");

        assertThatThrownBy(() -> Transaction.post(TransactionType.PAYROLL, List.of(companyEntry, employeeEntry), "payroll"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Currency must be equal");
    }
}
