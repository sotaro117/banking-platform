package com.example.ledger.pendingEvent;

import com.example.ledger.repository.PendingEventRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
public class PendingEventPoller {
    private static final Log log = LogFactory.getLog(PendingEventPoller.class);
    private PendingEventRepository pendingEventRepository;

    private KafkaTemplate<String, String> kafkaTemplate;

    public PendingEventPoller(PendingEventRepository pendingEventRepository, KafkaTemplate kafkaTemplate) {
        this.pendingEventRepository = pendingEventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 1000)
    public void publishEvent() {
        List<PendingEvent> unpublished = pendingEventRepository.findByPublishedFalseOrderByCreatedAtAsc();

        for (PendingEvent event: unpublished) {
            try {
                kafkaTemplate.send("ledger.entries", event.getAggregateId().toString(), event.getEntryPayload()).get();

                event.setPublished(true);
                pendingEventRepository.save(event);
            } catch (InterruptedException | ExecutionException e) {
                log.warn("Failed to publish event" + event.getId() + "retry next poll");
            }
        }
    }
}
