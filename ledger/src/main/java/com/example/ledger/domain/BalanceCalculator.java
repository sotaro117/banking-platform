package com.example.ledger.domain;

import com.example.ledger.domain.enums.AccountType;
import com.example.ledger.domain.enums.LedgerDirection;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

// reference
// Assets and Expenses:
//
//- Debit: Increases the balance.
//- Credit: Decreases the balance.
//
// Liabilities, Equity, and Revenue:
//
//- Debit: Decreases the balance.
//- Credit: Increases the balance.

// credit - debit == liability
// or debit - credit == asset

public class BalanceCalculator {
    private static final Set<AccountType> DEBIT_NORMAL = Set.of(AccountType.ASSET, AccountType.EXPENSE);

    public static BigDecimal compute(AccountType type, List<LedgerEntry> entries) {
        BigDecimal debit = sumByDirection(LedgerDirection.DEBIT, entries);
        BigDecimal credit = sumByDirection(LedgerDirection.CREDIT, entries);

        return DEBIT_NORMAL.contains(type) ? debit.subtract(credit) : credit.subtract(debit);
    }

    public static BigDecimal computeForWallet(Wallet wallet, AccountType type, List<LedgerEntry> entries) {
        List<LedgerEntry> entriesForWallet = entries.stream()
                .filter(e -> e.getWalletId().getId() == wallet.getId())
                .toList();

        return compute(type, entriesForWallet);
    }

    private static BigDecimal sumByDirection(LedgerDirection direction, List<LedgerEntry> entries) {
        return entries.stream()
                .filter(e -> e.getDirection() == direction)
                .map(LedgerEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
