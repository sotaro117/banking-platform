package com.example.ledger.pendingEvent;

import com.example.ledger.domain.Transaction;
import com.example.ledger.domain.enums.LedgerDirection;
import com.example.ledger.domain.enums.TransactionType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record LedgerEntryPayload(UUID transactionId, TransactionType type, List<Entry> entryList) {
    public static LedgerEntryPayload from(Transaction transaction) {

        List<Entry> entryPayload = transaction.getEntries()
                .stream()
                .map(e -> new Entry(e.getWallet().getId(), e.getDirection(), e.getAmount()))
                .toList();

        return new LedgerEntryPayload(transaction.getId(), transaction.getType(), entryPayload);
    }
}

record Entry(UUID walletId, LedgerDirection direction, BigDecimal amount) {}
