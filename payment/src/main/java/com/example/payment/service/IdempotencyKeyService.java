package com.example.payment.service;

import com.example.payment.domain.IdempotencyKey;
import com.example.payment.repository.IdempotencyKeyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IdempotencyKeyService {

    @Autowired
    private IdempotencyKeyRepository idempotencyKeyRepository;

    public IdempotencyKey isExsitsIdempotencyKey(String key) {
        Optional<IdempotencyKey> idempotencyKey = idempotencyKeyRepository.findByKey(key);
        return idempotencyKey.orElse(null);
    }

    public boolean validateKey(String key, String hash) {
        Optional<IdempotencyKey> idempotencyKey = idempotencyKeyRepository.findByKey(key);
        if (idempotencyKey.isPresent()) {
            if (idempotencyKey.get().getKey().equals(key) && idempotencyKey.get().getRequestHash().equals(hash)) {
                return true;
            } else {
                throw new IllegalArgumentException();
            }
        }
        return false;
    }
}
