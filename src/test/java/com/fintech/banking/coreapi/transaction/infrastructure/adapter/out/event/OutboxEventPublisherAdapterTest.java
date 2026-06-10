package com.fintech.banking.coreapi.transaction.infrastructure.adapter.out.event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fintech.banking.coreapi.transaction.domain.event.EventType;
import com.fintech.banking.coreapi.transaction.domain.event.TransactionPendingEvent;
import com.fintech.banking.coreapi.transaction.domain.model.AggregateType;
import com.fintech.banking.coreapi.transaction.domain.model.TransactionRecord;
import com.fintech.banking.coreapi.transaction.infrastructure.adapter.out.persistence.entity.OutboxEventJpaEntity;
import com.fintech.banking.coreapi.transaction.infrastructure.adapter.out.persistence.repository.SpringDataOutboxEventRepository;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class OutboxEventPublisherAdapterTest {

	@Mock
	private SpringDataOutboxEventRepository outboxRepository;

	@Mock
	private ObjectMapper objectMapper;

	@InjectMocks
	private OutboxEventPublisherAdapter adapter;

	@Test
	void shouldPublishEventSuccessfully() throws Exception {
		UUID eventId = UUID.randomUUID();
		String transactionId = "tx-123";
		TransactionPendingEvent event = new TransactionPendingEvent(eventId, transactionId, Instant.now(), "SRC", "TGT",
				new BigDecimal("100"), TransactionRecord.TransactionType.TRANSFER, "user");

		when(objectMapper.writeValueAsString(event)).thenReturn("{\"eventId\":\"" + eventId + "\"}");

		adapter.publish(event);

		ArgumentCaptor<OutboxEventJpaEntity> captor = ArgumentCaptor.forClass(OutboxEventJpaEntity.class);
		verify(outboxRepository).save(captor.capture());

		OutboxEventJpaEntity savedEntity = captor.getValue();
		assertEquals(eventId, savedEntity.getId());
		assertEquals(AggregateType.TRANSACTION, savedEntity.getAggregateType());
		assertEquals(transactionId, savedEntity.getAggregateId());
		assertEquals(EventType.TRANSACTION_PENDING, savedEntity.getEventType());
		assertEquals("{\"eventId\":\"" + eventId + "\"}", savedEntity.getPayload());
		assertEquals(
				com.fintech.banking.coreapi.transaction.infrastructure.adapter.out.persistence.entity.OutboxStatus.PENDING,
				savedEntity.getStatus());
		assertNotNull(savedEntity.getCreatedAt());
	}

	@Test
	void shouldThrowRuntimeExceptionWhenSerializationFails() throws Exception {
		UUID eventId = UUID.randomUUID();
		TransactionPendingEvent event = new TransactionPendingEvent(eventId, "tx-123", Instant.now(), "SRC", "TGT",
				new BigDecimal("100"), TransactionRecord.TransactionType.TRANSFER, "user");

		JacksonException jacksonException = mock(JacksonException.class);
		when(objectMapper.writeValueAsString(event)).thenThrow(jacksonException);

		RuntimeException exception = assertThrows(RuntimeException.class, () -> adapter.publish(event));
		assertEquals("Failed to serialize event: " + eventId, exception.getMessage());
		assertEquals(jacksonException, exception.getCause());

		verify(outboxRepository, never()).save(any());
	}
}
