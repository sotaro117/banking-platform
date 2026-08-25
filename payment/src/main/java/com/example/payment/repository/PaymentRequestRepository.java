package com.example.payment.repository;

import com.example.payment.domain.PaymentRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, UUID> {
    public PaymentRequest findByIdempotencyKey(String key);
}
