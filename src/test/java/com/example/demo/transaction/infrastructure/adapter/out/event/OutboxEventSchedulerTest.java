package com.example.demo.transaction.infrastructure.adapter.out.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.Message;

import com.example.demo.transaction.domain.event.EventType;
import com.example.demo.transaction.domain.event.TransactionCompletedEvent;
import com.example.demo.transaction.domain.event.TransactionFailedEvent;
import com.example.demo.transaction.domain.event.TransactionPendingEvent;
import com.example.demo.transaction.domain.model.AggregateType;
import com.example.demo.transaction.domain.model.TransactionRecord;
import com.example.demo.transaction.infrastructure.adapter.out.persistence.entity.OutboxEventJpaEntity;
import com.example.demo.transaction.infrastructure.adapter.out.persistence.entity.OutboxStatus;
import com.example.demo.transaction.infrastructure.adapter.out.persistence.repository.SpringDataOutboxEventRepository;

import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class OutboxEventSchedulerTest {

	@Mock
	private SpringDataOutboxEventRepository outboxRepository;

	@Mock
	private KafkaTemplate<String, String> kafkaTemplate;

	@Mock
	private ObjectMapper objectMapper;

	@InjectMocks
	private OutboxEventScheduler scheduler;

	@Test
	void shouldDoNothingWhenNoPendingEvents() {
		when(outboxRepository.findByStatusOrderByCreatedAtAscWithLock(eq(OutboxStatus.PENDING), any(Pageable.class)))
				.thenReturn(Collections.emptyList());

		scheduler.publishPendingEvents();

		verify(kafkaTemplate, never()).send(any(Message.class));
		verify(outboxRepository, never()).save(any());
	}

	@Test
	void shouldPublishPendingEvents() throws Exception {
		UUID eventId = UUID.randomUUID();
		String transactionId = UUID.randomUUID().toString();
		TransactionPendingEvent event = new TransactionPendingEvent(eventId, transactionId, Instant.now(), "SRC123",
				"TGT456", new BigDecimal("100.00"), TransactionRecord.TransactionType.TRANSFER, "user1");

		when(objectMapper.readValue("{}", TransactionPendingEvent.class)).thenReturn(event);

		OutboxEventJpaEntity entity = new OutboxEventJpaEntity(eventId, AggregateType.TRANSACTION, transactionId,
				EventType.TRANSACTION_PENDING, "{}", Instant.now(), OutboxStatus.PENDING, 0);

		when(outboxRepository.findByStatusOrderByCreatedAtAscWithLock(eq(OutboxStatus.PENDING), any(Pageable.class)))
				.thenReturn(List.of(entity));
		when(kafkaTemplate.send(any(Message.class))).thenReturn(CompletableFuture.completedFuture(null));

		scheduler.publishPendingEvents();

		verify(kafkaTemplate, times(1)).send(any(Message.class));
		assertEquals(OutboxStatus.PUBLISHED, entity.getStatus());
		verify(outboxRepository).save(entity);
	}

	@Test
	void shouldPublishCompletedEventAndMarkAsPublished() throws Exception {
		OutboxEventJpaEntity entity = OutboxEventJpaEntity.builder().id(UUID.randomUUID())
				.aggregateType(AggregateType.TRANSACTION).aggregateId("tx1").eventType(EventType.TRANSACTION_COMPLETED)
				.payload("{}").createdAt(Instant.now()).status(OutboxStatus.PENDING).retryCount(0).build();

		when(outboxRepository.findByStatusOrderByCreatedAtAscWithLock(eq(OutboxStatus.PENDING), any(Pageable.class)))
				.thenReturn(List.of(entity));

		TransactionCompletedEvent event = new TransactionCompletedEvent(UUID.randomUUID(), "tx1", Instant.now());
		when(objectMapper.readValue("{}", TransactionCompletedEvent.class)).thenReturn(event);

		when(kafkaTemplate.send(any(Message.class))).thenReturn(CompletableFuture.completedFuture(null));

		scheduler.publishPendingEvents();

		verify(kafkaTemplate, times(1)).send(any(Message.class));
		assertEquals(OutboxStatus.PUBLISHED, entity.getStatus());
		verify(outboxRepository).save(entity);
	}

	@Test
	void shouldPublishFailedEventAndMarkAsPublished() throws Exception {
		OutboxEventJpaEntity entity = OutboxEventJpaEntity.builder().id(UUID.randomUUID())
				.aggregateType(AggregateType.TRANSACTION).aggregateId("tx1").eventType(EventType.TRANSACTION_FAILED)
				.payload("{}").createdAt(Instant.now()).status(OutboxStatus.PENDING).retryCount(0).build();

		when(outboxRepository.findByStatusOrderByCreatedAtAscWithLock(eq(OutboxStatus.PENDING), any(Pageable.class)))
				.thenReturn(List.of(entity));

		TransactionFailedEvent event = new TransactionFailedEvent(UUID.randomUUID(), "tx1", Instant.now(), "err");
		when(objectMapper.readValue("{}", TransactionFailedEvent.class)).thenReturn(event);

		when(kafkaTemplate.send(any(Message.class))).thenReturn(CompletableFuture.completedFuture(null));

		scheduler.publishPendingEvents();

		verify(kafkaTemplate, times(1)).send(any(Message.class));
		assertEquals(OutboxStatus.PUBLISHED, entity.getStatus());
		verify(outboxRepository).save(entity);
	}

	@Test
	void shouldIncrementRetryCountOnFailure() throws Exception {
		OutboxEventJpaEntity entity = OutboxEventJpaEntity.builder().id(UUID.randomUUID())
				.aggregateType(AggregateType.TRANSACTION).aggregateId("tx1").eventType(EventType.TRANSACTION_PENDING)
				.payload("{}").createdAt(Instant.now()).status(OutboxStatus.PENDING).retryCount(1).build();

		when(outboxRepository.findByStatusOrderByCreatedAtAscWithLock(eq(OutboxStatus.PENDING), any(Pageable.class)))
				.thenReturn(List.of(entity));
		when(objectMapper.readValue("{}", TransactionPendingEvent.class))
				.thenThrow(new RuntimeException("Deserialization error"));

		scheduler.publishPendingEvents();

		verify(kafkaTemplate, never()).send(any(Message.class));
		assertEquals(OutboxStatus.PENDING, entity.getStatus());
		assertEquals(2, entity.getRetryCount());
		verify(outboxRepository).save(entity);
	}

	@Test
	void shouldMarkAsFailedWhenMaxRetriesReached() throws Exception {
		OutboxEventJpaEntity entity = OutboxEventJpaEntity.builder().id(UUID.randomUUID())
				.aggregateType(AggregateType.TRANSACTION).aggregateId("tx1").eventType(EventType.TRANSACTION_PENDING)
				.payload("{}").createdAt(Instant.now()).status(OutboxStatus.PENDING)
				.retryCount(OutboxEventScheduler.MAX_RETRIES - 1).build();

		when(outboxRepository.findByStatusOrderByCreatedAtAscWithLock(eq(OutboxStatus.PENDING), any(Pageable.class)))
				.thenReturn(List.of(entity));

		scheduler.publishPendingEvents();

		assertEquals(OutboxStatus.FAILED, entity.getStatus());
		assertEquals(OutboxEventScheduler.MAX_RETRIES, entity.getRetryCount());
		verify(outboxRepository).save(entity);
	}

	@Test
	void shouldPassBatchSizeToRepository() {
		when(outboxRepository.findByStatusOrderByCreatedAtAscWithLock(eq(OutboxStatus.PENDING), any(Pageable.class)))
				.thenReturn(Collections.emptyList());

		scheduler.publishPendingEvents();

		verify(outboxRepository).findByStatusOrderByCreatedAtAscWithLock(eq(OutboxStatus.PENDING),
				eq(PageRequest.of(0, scheduler.batchSize)));
	}
	@Test
	void shouldIgnoreDatabaseShutdownExceptions() {
		when(outboxRepository.findByStatusOrderByCreatedAtAscWithLock(eq(OutboxStatus.PENDING), any(Pageable.class)))
				.thenThrow(new org.springframework.dao.DataAccessResourceFailureException("DB is closing"));

		scheduler.publishPendingEvents();

		verify(kafkaTemplate, never()).send(any(Message.class));
		verify(outboxRepository, never()).save(any());
	}
}
