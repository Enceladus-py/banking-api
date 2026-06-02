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

/**
 * Polls the outbox table for PENDING events and dispatches them to the local
 * Spring {@link ApplicationEventPublisher}.
 *
 * <p><strong>Retry policy</strong>: each event is retried up to
 * {@value #MAX_RETRIES} times. On exhaustion the event is moved to
 * {@link OutboxStatus#FAILED} so it no longer blocks subsequent events and is
 * visible for manual inspection / alerting.
 *
 * <p><strong>Batch cap</strong>: at most {@value #BATCH_SIZE} events are
 * processed per tick to bound the work done per scheduler invocation.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventScheduler {

    /** Maximum number of dispatch attempts before an event is parked as FAILED. */
    static final int MAX_RETRIES = 5;

    /** Maximum number of PENDING events processed per scheduler tick. */
    static final int BATCH_SIZE = 50;

    private final SpringDataOutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher localEventPublisher;

    @Scheduled(fixedDelay = 100) // Poll every 100 milliseconds
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEventJpaEntity> pendingEvents = outboxRepository
                .findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);

        if (pendingEvents.isEmpty()) {
            return;
        }

        // Cap the batch to avoid unbounded work per tick
        List<OutboxEventJpaEntity> batch = pendingEvents.size() > BATCH_SIZE
                ? pendingEvents.subList(0, BATCH_SIZE)
                : pendingEvents;

        log.info("Found {} pending outbox events; processing up to {} this tick",
                pendingEvents.size(), batch.size());

        for (OutboxEventJpaEntity entity : batch) {
            try {
                TransactionEvent event = deserialize(entity);
                localEventPublisher.publishEvent(event);

                // Mark as PUBLISHED so it is not processed again
                entity.setStatus(OutboxStatus.PUBLISHED);
                outboxRepository.save(entity);

            } catch (Exception e) {
                int attempts = entity.getRetryCount() + 1;
                entity.setRetryCount(attempts);

                if (attempts >= MAX_RETRIES) {
                    log.error("Outbox event {} has failed {} times — marking as FAILED. Manual intervention required. Error: {}",
                            entity.getId(), attempts, e.getMessage(), e);
                    entity.setStatus(OutboxStatus.FAILED);
                } else {
                    log.warn("Outbox event {} failed (attempt {}/{}). Will retry on next tick. Error: {}",
                            entity.getId(), attempts, MAX_RETRIES, e.getMessage());
                }

                outboxRepository.save(entity);
            }
        }
    }

    private TransactionEvent deserialize(OutboxEventJpaEntity entity) throws Exception {
        String eventType = entity.getEventType();
        String payload = entity.getPayload();

        return switch (eventType) {
            case "TransactionPendingEvent"   -> objectMapper.readValue(payload, TransactionPendingEvent.class);
            case "TransactionCompletedEvent" -> objectMapper.readValue(payload, TransactionCompletedEvent.class);
            case "TransactionFailedEvent"    -> objectMapper.readValue(payload, TransactionFailedEvent.class);
            default -> throw new IllegalArgumentException("Unknown event type: " + eventType);
        };
    }
}
