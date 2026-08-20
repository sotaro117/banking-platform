package com.example.ledger.controller;

import com.example.ledger.domain.LedgerEntry;
import com.example.ledger.domain.Wallet;
import com.example.ledger.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/wallet")
public class WalletController {
    @Autowired
    private WalletService walletService;

    @PostMapping("/create")
    public ResponseEntity<Void> createWallet(@RequestBody Wallet wallet) {
        walletService.saveWallet(wallet);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable UUID id) {
        BigDecimal balance = walletService.getBalanceFromEntries(id);

        if (balance == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().body(balance);
    }

    @GetMapping("/{id}/entry")
    public ResponseEntity<List<LedgerEntry>> getEntryHistoryWithinDate(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate
    ) {
        return null;
    }

    // test
    @GetMapping("/{id}")
    public ResponseEntity<Wallet> getWallet(@PathVariable UUID id) {
        Wallet wallet = walletService.getWalletById(id);
        return ResponseEntity.ok().body(wallet);
    }
}
