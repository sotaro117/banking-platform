package com.example.ledger;

import com.example.ledger.domain.LedgerEntry;
import com.example.ledger.domain.Party;
import com.example.ledger.domain.Transaction;
import com.example.ledger.domain.Wallet;
import com.example.ledger.domain.enums.PartyStatus;
import com.example.ledger.domain.enums.PartyType;
import com.example.ledger.domain.enums.TransactionType;
import com.example.ledger.repository.TransactionRepository;
import com.example.ledger.service.TransactionService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void pointingATransactionSavesAllEntriesAndTheTransaction() {
        Party companyParty = new Party(UUID.randomUUID(), "external-reference", PartyType.COMPANY, "company", PartyStatus.ACTIVE);
        Party employeeParty = new Party(UUID.randomUUID(), "external-reference", PartyType.EMPLOYEE, "Jane Doe", PartyStatus.ACTIVE);

        Wallet companyWallet = Wallet.linkExpense(UUID.randomUUID(), companyParty, "EUR");
        Wallet employeeWallet = Wallet.linkLiability(UUID.randomUUID(), employeeParty, "EUR");

        LedgerEntry companyEntry = LedgerEntry.debit(companyWallet, new BigDecimal(100), "EUR");
        LedgerEntry employeeEntry = LedgerEntry.credit(employeeWallet, new BigDecimal(100), "EUR");

        Transaction tx = Transaction.post(TransactionType.PAYROLL, List.of(companyEntry, employeeEntry), "payroll");

        given(transactionRepository.save(tx)).willReturn(tx);

        Transaction savedTx = transactionService.saveTransaction(tx);

        then(transactionRepository)
                .should()
                .save(savedTx);
    }

    @Test
    void postingAnUnbalancedTransactionNeverCallsTheRepository() {
        Party companyParty = new Party(UUID.randomUUID(), "external-reference", PartyType.COMPANY, "company", PartyStatus.ACTIVE);
        Party employeeParty = new Party(UUID.randomUUID(), "external-reference", PartyType.EMPLOYEE, "Jane Doe", PartyStatus.ACTIVE);

        Wallet companyWallet = Wallet.linkExpense(UUID.randomUUID(), companyParty, "EUR");
        Wallet employeeWallet = Wallet.linkLiability(UUID.randomUUID(), employeeParty, "EUR");

        LedgerEntry companyEntry = LedgerEntry.debit(companyWallet, new BigDecimal(50), "EUR");
        LedgerEntry employeeEntry = LedgerEntry.credit(employeeWallet, new BigDecimal(100), "EUR");

        Transaction transaction = Transaction.post(TransactionType.PAYROLL, List.of(companyEntry, employeeEntry), "payroll");

        assertThatThrownBy(() -> transactionService.saveTransaction(transaction) )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Transaction balance must be equal");

        then(transactionRepository)
                .should(never())
                .save(transaction);
    }
}
