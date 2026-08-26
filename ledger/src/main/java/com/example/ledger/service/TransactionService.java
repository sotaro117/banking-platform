package com.example.ledger.service;

import com.example.ledger.domain.LedgerEntry;
import com.example.ledger.domain.Transaction;
import com.example.ledger.domain.Wallet;
import com.example.ledger.domain.enums.TransactionType;
import com.example.ledger.pendingEvent.PendingEvent;
import com.example.ledger.repository.PendingEventRepository;
import com.example.ledger.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private WalletService walletService;
    @Autowired
    private PendingEventRepository pendingEventRepository;

    public Transaction saveTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    @Transactional(rollbackFor = Exception.class)
    public void makeTransaction(TransactionType type, UUID creditWalletId, UUID debitWalletId, BigDecimal amount, String currency) {
        Wallet creditWallet = walletService.getWalletById(creditWalletId);
        Wallet debitWallet = walletService.getWalletById(debitWalletId);

        LedgerEntry credit = LedgerEntry.credit(creditWallet, amount, currency);
        LedgerEntry debit = LedgerEntry.debit(debitWallet, amount, currency);
        Transaction postTransaction = Transaction.post(type, List.of(credit, debit), "");

        saveTransaction(postTransaction);

        ObjectMapper objectMapper = new ObjectMapper();
        PendingEvent event = PendingEvent.eventForTransaction(postTransaction, objectMapper);
        pendingEventRepository.save(event);
    }
}
