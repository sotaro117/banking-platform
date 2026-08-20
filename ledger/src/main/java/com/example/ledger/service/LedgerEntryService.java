package com.example.ledger.service;

import com.example.ledger.domain.LedgerEntry;
import com.example.ledger.repository.LedgerEntryRepository;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class LedgerEntryService {
    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    public LedgerEntry saveEntry(@NonNull LedgerEntry ledgerEntry) {
        if (ledgerEntry.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be set more than 1");
        }

        return ledgerEntryRepository.save(ledgerEntry);
    }
}
