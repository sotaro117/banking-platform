package com.example.ledger.service;

import com.example.ledger.domain.Wallet;
import com.example.ledger.repository.WalletRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WalletService {
    @Autowired
    private WalletRepository walletRepository;

    public Wallet saveWallet(@NonNull Wallet wallet) {
        return walletRepository.save(wallet);
    }
}
