package com.example.ledger.service;

import com.example.ledger.domain.BalanceCalculator;
import com.example.ledger.domain.LedgerEntry;
import com.example.ledger.domain.Wallet;
import com.example.ledger.domain.enums.AccountType;
import com.example.ledger.repository.WalletRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class WalletService {
    @Autowired
    private WalletRepository walletRepository;

    public Wallet saveWallet(@NonNull Wallet wallet) {
        return walletRepository.save(wallet);
    }

    public Wallet getWalletById(@NonNull UUID id) {
        Optional<Wallet> wallet = walletRepository.findById(id);
        return wallet.orElse(null);
    }

    public BigDecimal getBalanceFromEntries(UUID id) {
        Optional<Wallet> wallet = walletRepository.findById(id);
        if (wallet.isEmpty()) {
            return null;
        }
        List<LedgerEntry> entries = wallet.get().getEntries();

        return BalanceCalculator.computeForWallet(wallet.get(), AccountType.LIABILITY, entries);
    }
}
