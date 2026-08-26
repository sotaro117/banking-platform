package com.example.ledger;

import com.example.ledger.domain.LedgerEntry;
import com.example.ledger.domain.Party;
import com.example.ledger.domain.Transaction;
import com.example.ledger.domain.Wallet;
import com.example.ledger.domain.enums.PartyStatus;
import com.example.ledger.domain.enums.PartyType;
import com.example.ledger.domain.enums.TransactionType;
import com.example.ledger.pendingEvent.PendingEvent;
import com.example.ledger.repository.PendingEventRepository;
import com.example.ledger.repository.TransactionRepository;
import com.example.ledger.service.TransactionService;
import com.example.ledger.service.WalletService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.BDDMockito.*;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class PendingEventTest {
    @Mock
    private PendingEventRepository pendingEventRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void shouldSavePendingEventWhenPostingTransaction() {
        Party company = new Party(UUID.randomUUID(), "", PartyType.COMPANY, "company", PartyStatus.ACTIVE);
        Party employee = new Party(UUID.randomUUID(), "HR-external-registration", PartyType.EMPLOYEE, "company", PartyStatus.ACTIVE);

        UUID companyWalletId = UUID.randomUUID();
        UUID employeeWalletId = UUID.randomUUID();
        Wallet companyWallet = Wallet.linkAsset(companyWalletId, company, "EUR");
        Wallet employeeWallet = Wallet.linkLiability(employeeWalletId, employee, "EUR");

        LedgerEntry companyEntry = LedgerEntry.debit(companyWallet, new BigDecimal(100), "EUR");
        LedgerEntry employeeEntry = LedgerEntry.credit(employeeWallet, new BigDecimal(100), "EUR");
        Transaction postTransaction = Transaction.post(TransactionType.PAYROLL, List.of(companyEntry, employeeEntry), "payroll");

        given(transactionRepository.save(postTransaction))
                .willReturn(postTransaction);
        given(walletService.getWalletById(companyWalletId))
                .willReturn(companyWallet);
        given(walletService.getWalletById(employeeWalletId))
                .willReturn(employeeWallet);

        transactionService.saveTransaction(postTransaction);

        transactionService.makeTransaction(postTransaction.getType(), employeeWalletId, companyWalletId, new BigDecimal(100), "EUR");

        then(pendingEventRepository)
                .should()
                .save(any(PendingEvent.class));
    }
}
