package com.example.ledger.controller;

import com.example.ledger.domain.LedgerEntry;
import com.example.ledger.domain.PostTransactionRequest;
import com.example.ledger.domain.Transaction;
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

import java.util.UUID;

@RestController
@RequestMapping("/internal/transaction")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;

    @PostMapping
    public ResponseEntity<Void> internalTransaction(@RequestBody PostTransactionRequest request) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(request.getExternalAccount());
        String debitAccountId = jsonNode.get("walletReference").toString();
        transactionService.makeTransaction(request.getRequestType(), request.getCreditAccount(), UUID.fromString(debitAccountId), request.getAmount(), request.getCurrency());

        return ResponseEntity.ok().build();
    }
}
