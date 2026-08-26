package com.example.ledger.repository;

import com.example.ledger.pendingEvent.PendingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PendingEventRepository extends JpaRepository<PendingEvent, UUID> {
    public List<PendingEvent> findByPublishedFalseOrderByCreatedAtAsc();
}
