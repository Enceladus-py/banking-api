package com.example.demo.infrastructure.adapter.out.event;

import com.example.demo.domain.event.TransactionCompletedEvent;
import com.example.demo.domain.event.TransactionFailedEvent;
import com.example.demo.domain.event.TransactionPendingEvent;
import com.example.demo.infrastructure.adapter.out.persistence.entity.OutboxEventJpaEntity;
import com.example.demo.infrastructure.adapter.out.persistence.entity.OutboxStatus;
import com.example.demo.infrastructure.adapter.out.persistence.repository.SpringDataOutboxEventRepository;
import com.example.demo.domain.model.TransactionRecord.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxEventSchedulerTest {

    @Mock
    private SpringDataOutboxEventRepository outboxRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ApplicationEventPublisher localEventPublisher;

    private OutboxEventScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new OutboxEventScheduler(outboxRepository, objectMapper, localEventPublisher);
    }

    @Test
    void shouldDoNothingWhenNoPendingEvents() {
        when(outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING))
                .thenReturn(Collections.emptyList());

        scheduler.publishPendingEvents();

        verify(localEventPublisher, never()).publishEvent(any());
        verify(outboxRepository, never()).save(any());
    }

    @Test
    void shouldPublishPendingEventAndMarkAsPublished() throws Exception {
        OutboxEventJpaEntity entity = OutboxEventJpaEntity.builder()
                .id(UUID.randomUUID())
                .aggregateType("Transaction")
                .aggregateId("tx1")
                .eventType("TransactionPendingEvent")
                .payload("{}")
                .createdAt(LocalDateTime.now())
                .status(OutboxStatus.PENDING)
                .retryCount(0)
                .build();

        when(outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING))
                .thenReturn(List.of(entity));

        TransactionPendingEvent event = new TransactionPendingEvent(
                UUID.randomUUID(), "tx1", Instant.now(), null, "acc1",
                BigDecimal.TEN, TransactionType.DEPOSIT, "u1"
        );
        when(objectMapper.readValue("{}", TransactionPendingEvent.class)).thenReturn(event);

        scheduler.publishPendingEvents();

        verify(localEventPublisher).publishEvent(event);
        assertEquals(OutboxStatus.PUBLISHED, entity.getStatus());
        verify(outboxRepository).save(entity);
    }

    @Test
    void shouldPublishCompletedEventAndMarkAsPublished() throws Exception {
        OutboxEventJpaEntity entity = OutboxEventJpaEntity.builder()
                .id(UUID.randomUUID())
                .aggregateType("Transaction")
                .aggregateId("tx1")
                .eventType("TransactionCompletedEvent")
                .payload("{}")
                .createdAt(LocalDateTime.now())
                .status(OutboxStatus.PENDING)
                .retryCount(0)
                .build();

        when(outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING))
                .thenReturn(List.of(entity));

        TransactionCompletedEvent event = new TransactionCompletedEvent(
                UUID.randomUUID(), "tx1", Instant.now()
        );
        when(objectMapper.readValue("{}", TransactionCompletedEvent.class)).thenReturn(event);

        scheduler.publishPendingEvents();

        verify(localEventPublisher).publishEvent(event);
        assertEquals(OutboxStatus.PUBLISHED, entity.getStatus());
        verify(outboxRepository).save(entity);
    }

    @Test
    void shouldPublishFailedEventAndMarkAsPublished() throws Exception {
        OutboxEventJpaEntity entity = OutboxEventJpaEntity.builder()
                .id(UUID.randomUUID())
                .aggregateType("Transaction")
                .aggregateId("tx1")
                .eventType("TransactionFailedEvent")
                .payload("{}")
                .createdAt(LocalDateTime.now())
                .status(OutboxStatus.PENDING)
                .retryCount(0)
                .build();

        when(outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING))
                .thenReturn(List.of(entity));

        TransactionFailedEvent event = new TransactionFailedEvent(
                UUID.randomUUID(), "tx1", Instant.now(), "err"
        );
        when(objectMapper.readValue("{}", TransactionFailedEvent.class)).thenReturn(event);

        scheduler.publishPendingEvents();

        verify(localEventPublisher).publishEvent(event);
        assertEquals(OutboxStatus.PUBLISHED, entity.getStatus());
        verify(outboxRepository).save(entity);
    }

    @Test
    void shouldIncrementRetryCountOnFailure() throws Exception {
        OutboxEventJpaEntity entity = OutboxEventJpaEntity.builder()
                .id(UUID.randomUUID())
                .aggregateType("Transaction")
                .aggregateId("tx1")
                .eventType("TransactionPendingEvent")
                .payload("{}")
                .createdAt(LocalDateTime.now())
                .status(OutboxStatus.PENDING)
                .retryCount(1)
                .build();

        when(outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING))
                .thenReturn(List.of(entity));
        when(objectMapper.readValue("{}", TransactionPendingEvent.class))
                .thenThrow(new RuntimeException("Deserialization error"));

        scheduler.publishPendingEvents();

        verify(localEventPublisher, never()).publishEvent(any());
        assertEquals(OutboxStatus.PENDING, entity.getStatus());
        assertEquals(2, entity.getRetryCount());
        verify(outboxRepository).save(entity);
    }

    @Test
    void shouldMarkAsFailedWhenMaxRetriesReached() throws Exception {
        OutboxEventJpaEntity entity = OutboxEventJpaEntity.builder()
                .id(UUID.randomUUID())
                .aggregateType("Transaction")
                .aggregateId("tx1")
                .eventType("UnknownEvent")
                .payload("{}")
                .createdAt(LocalDateTime.now())
                .status(OutboxStatus.PENDING)
                .retryCount(OutboxEventScheduler.MAX_RETRIES - 1)
                .build();

        when(outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING))
                .thenReturn(List.of(entity));

        scheduler.publishPendingEvents();

        assertEquals(OutboxStatus.FAILED, entity.getStatus());
        assertEquals(OutboxEventScheduler.MAX_RETRIES, entity.getRetryCount());
        verify(outboxRepository).save(entity);
    }

    @Test
    void shouldProcessOnlyBatchSizeEvents() {
        List<OutboxEventJpaEntity> pendingEvents = new ArrayList<>();
        for (int i = 0; i < OutboxEventScheduler.BATCH_SIZE + 10; i++) {
            OutboxEventJpaEntity e = OutboxEventJpaEntity.builder()
                    .id(UUID.randomUUID())
                    .aggregateType("Transaction")
                    .aggregateId("tx" + i)
                    .eventType("UnknownEvent")
                    .payload("{}")
                    .createdAt(LocalDateTime.now())
                    .status(OutboxStatus.PENDING)
                    .retryCount(0)
                    .build();
            pendingEvents.add(e);
        }

        when(outboxRepository.findByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING))
                .thenReturn(pendingEvents);

        scheduler.publishPendingEvents();

        verify(outboxRepository, times(OutboxEventScheduler.BATCH_SIZE)).save(any());
    }
}
