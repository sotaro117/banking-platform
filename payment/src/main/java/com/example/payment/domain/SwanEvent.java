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
    @Getter
    @Id
    private String id;

    @Getter
    @Column(name = "event_type")
    private String eventType;

    @Getter @Setter
    @UpdateTimestamp
    @Column(name = "processed_at")
    private Instant processedAt;


    public SwanEvent(String id, String eventType, Instant processedAt) {
        this.id = id;
        this.eventType = eventType;
        this.processedAt = processedAt;
    }


    protected SwanEvent() {
    }
}
