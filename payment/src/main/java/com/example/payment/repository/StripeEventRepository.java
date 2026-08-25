package com.example.payment.repository;

import com.example.payment.domain.StripeEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StripeEventRepository extends JpaRepository<StripeEvent, String> {
}
