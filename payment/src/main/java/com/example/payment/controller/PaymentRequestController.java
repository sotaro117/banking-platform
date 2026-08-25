package com.example.payment.controller;

import com.example.payment.domain.PaymentRequest;
import com.example.payment.service.PaymentRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/payment")
public class PaymentRequestController {

    private final PaymentRequestService paymentRequestService;

    public PaymentRequestController(PaymentRequestService paymentRequestService) {
        this.paymentRequestService = paymentRequestService;
    }

    @PostMapping
    ResponseEntity<Void> createPaymentRequest(@RequestHeader("Idempotency-Key") String idempotencyKey, @RequestBody PaymentRequest request) {
        paymentRequestService.createRequest(idempotencyKey, request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    ResponseEntity<PaymentRequest> getPaymentRequest(@PathVariable UUID id) {
        PaymentRequest request = paymentRequestService.getRequestById(id);
        if (request == null) { return ResponseEntity.notFound().build(); }
        return ResponseEntity.ok().body(request);
    }
}
