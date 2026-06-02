package com.example.demo.infrastructure.adapter.out.event;

import com.example.demo.domain.event.TransactionCompletedEvent;
import com.example.demo.domain.event.TransactionEvent;
import com.example.demo.domain.event.TransactionFailedEvent;
import com.example.demo.domain.event.TransactionPendingEvent;
import com.example.demo.infrastructure.adapter.out.persistence.entity.OutboxEventJpaEntity;
import com.example.demo.infrastructure.adapter.out.persistence.repository.SpringDataOutboxEventRepository;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import com.example.demo.infrastructure.adapter.out.persistence.entity.OutboxStatus;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventScheduler {

    private final SpringDataOutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher localEventPublisher;

    @Scheduled(fixedDelay = 100) // Poll every 100 milliseconds
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEventJpaEntity> pendingEvents = outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        if (pendingEvents.isEmpty()) {
            return;
        }

        log.info("Found {} pending outbox events to publish", pendingEvents.size());

        for (OutboxEventJpaEntity entity : pendingEvents) {
            try {
                TransactionEvent event = deserialize(entity);
                // Publish internally to Spring Context (local broker)
                localEventPublisher.publishEvent(event);
                
                // Mark as published so it is not processed again
                entity.setStatus(OutboxStatus.PUBLISHED);
                outboxRepository.save(entity);
            } catch (Exception e) {
                log.error("Failed to publish outbox event: " + entity.getId(), e);
            }
        }
    }

    private TransactionEvent deserialize(OutboxEventJpaEntity entity) throws Exception {
        String eventType = entity.getEventType();
        String payload = entity.getPayload();
        
        return switch (eventType) {
            case "TransactionPendingEvent" -> objectMapper.readValue(payload, TransactionPendingEvent.class);
            case "TransactionCompletedEvent" -> objectMapper.readValue(payload, TransactionCompletedEvent.class);
            case "TransactionFailedEvent" -> objectMapper.readValue(payload, TransactionFailedEvent.class);
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };
    }
}
