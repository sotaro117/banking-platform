package com.example.ledger.repository;

import com.example.ledger.domain.LedgerEntry;
import com.example.ledger.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> { }
