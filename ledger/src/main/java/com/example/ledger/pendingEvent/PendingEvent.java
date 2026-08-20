package com.example.ledger.pendingEvent;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

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

@Entity
public class PendingEvent {
    @Id
    private UUID id;

    @Column(name = "aggregate_id")
    private UUID aggregateId;

    @Column(name = "pending_event_type")
    private String pendingEventType;

    @Convert(converter = EventPayloadAttributeConverter.class)
    @Column(name = "event_type")
    private EventPayload payload;

    private boolean published;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}
