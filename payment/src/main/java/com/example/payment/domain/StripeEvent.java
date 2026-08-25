package com.example.payment.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(name = "stripe_event")
public class StripeEvent {
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


    public StripeEvent(String id, String eventType, Instant processedAt) {
        this.id = id;
        this.eventType = eventType;
        this.processedAt = processedAt;
    }


    protected StripeEvent() {
    }
}
