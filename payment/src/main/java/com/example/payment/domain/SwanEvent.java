package com.example.payment.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

// store swan webhook
@Entity
@Table(name = "swan_event")
public class SwanEvent {
    @Getter @Setter
    @Id
    private String id;

    @Getter @Setter
    @Column(name = "event_type")
    private String eventType;

    @Getter @Setter
    @Column(name = "resource_id")
    private String resourceId;

    @Getter @Setter
    @Column(name = "processed_at")
    private Instant processedAt;


    public SwanEvent(String id, String eventType, String resourceId, Instant processedAt) {
        this.id = id;
        this.eventType = eventType;
        this.resourceId = resourceId;
        this.processedAt = processedAt;
    }


    protected SwanEvent() {
    }
}
