package com.example.payment.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "idempotency_key")
public class IdempotencyKey {
    @Getter @Setter
    @Id
    private String key;

    @Getter @Setter
    @Column(name = "request_hash")
    private String requestHash;

    @Getter @Setter
    @Column(name = "response_snapshot")
    private String responseSnapshot;

    @Getter
    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;

    @Getter @Setter
    @UpdateTimestamp
    @Column(name = "expires_at")
    private Instant expiresAt;


    public IdempotencyKey(String key, String requestHash, String responseSnapshot, Instant createdAt, Instant expiresAt) {
        this.key = key;
        this.requestHash = requestHash;
        this.responseSnapshot = responseSnapshot;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }


    protected IdempotencyKey() {
    }
}
