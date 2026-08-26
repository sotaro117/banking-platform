package com.example.ledger.pendingEvent;

import com.example.ledger.domain.LedgerEntry;
import com.example.ledger.domain.Transaction;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

// | Column | Type | Notes |
//|---|---|---|
//| id | UUID (PK) | |
//| aggregate_id | UUID | e.g. transaction_id |
//| event_type | VARCHAR | e.g. `LEDGER_ENTRY_POSTED` |
//| payload | JSONB | |
//| published | BOOLEAN | flipped by the outbox poller once sent to Kafka |
//| created_at | TIMESTAMPTZ | |

@Entity @Data
public class PendingEvent {
    @Id
    private UUID id;

    @Column(name = "aggregate_id")
    private UUID aggregateId;

    @Column(name = "pending_event_type")
    private String pendingEventType;

    @Column(name = "entry_payload")
    private String entryPayload;

    private boolean published;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;


    private PendingEvent(UUID id, UUID aggregateId, String pendingEventType, String entryPayload, boolean published, Instant createdAt) {
        this.id = id;
        this.aggregateId = aggregateId;
        this.pendingEventType = pendingEventType;
        this.entryPayload = entryPayload;
        this.published = published;
        this.createdAt = createdAt;
    }

    protected PendingEvent() {
    }

    public static PendingEvent eventForTransaction(Transaction transaction, ObjectMapper objectMapper) {
        LedgerEntryPayload payload = LedgerEntryPayload.from(transaction);
        String payloadJson;

        payloadJson = objectMapper.writeValueAsString(payload);

        return new PendingEvent(UUID.randomUUID(), transaction.getId(), "LEGER_ENTRY_POSTED", payloadJson, false, Instant.now());
    }
}
