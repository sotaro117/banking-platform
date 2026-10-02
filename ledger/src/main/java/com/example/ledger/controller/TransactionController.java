package com.example.ledger.controller;

import com.example.ledger.domain.LedgerEntry;
import com.example.ledger.domain.Transaction;
import com.example.ledger.domain.enums.TransactionType;
import com.example.ledger.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/internal/transaction")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;

    @PostMapping
    public ResponseEntity<Void> internalTransaction(@RequestBody Map<String, Object> request) {
        String requestType = (String) request.get("requestType");
        TransactionType type = TransactionType.valueOf(requestType);

        Map<String, Object> creditAccount = (Map<String, Object>) request.get("creditAccount");
        UUID creditWallet = UUID.fromString((String) creditAccount.get("walletReference"));

        Map<String, Object> debitAccount = (Map<String, Object>) request.get("debitAccount");
        UUID debitWallet = UUID.fromString((String) debitAccount.get("walletReference"));

        BigDecimal amount = new BigDecimal((int) request.get("amount"));

        String currency = (String) request.get("currency");

        String paymentState = (String) request.get("paymentState");

        if (type == TransactionType.REVERSAL) {
            UUID transactionId = UUID.fromString((String) request.get("ledgerTransactionId"));
            transactionService.reverseTransaction(transactionId, type, creditWallet, debitWallet, amount, currency);
        } else if (paymentState.equalsIgnoreCase("SETTLED")) {
            UUID transactionId = UUID.fromString((String) request.get("ledgerTransactionId"));
            transactionService.settleTransaction(transactionId);
        } else {
            transactionService.makeTransaction(type, creditWallet, debitWallet, amount, currency);
        }

        return ResponseEntity.ok().build();
    }
}
