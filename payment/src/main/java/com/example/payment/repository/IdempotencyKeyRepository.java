package com.example.payment.repository;

import com.example.payment.domain.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, String> {
    public Optional<IdempotencyKey> findByKey(String key);
}
