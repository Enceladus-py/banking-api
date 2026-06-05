package com.example.demo.transaction.infrastructure.adapter.in.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.transaction.application.port.in.ProcessTransactionUseCase;
import com.example.demo.transaction.domain.event.EventType;
import com.example.demo.transaction.domain.event.TransactionPendingEvent;
import com.example.demo.transaction.domain.model.TransactionRecord;

import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class KafkaTransactionEventListenerTest {

	@Mock
	private ProcessTransactionUseCase processTransactionUseCase;

	@Mock
	private ObjectMapper objectMapper;

	@InjectMocks
	private KafkaTransactionEventListener listener;

	@Test
	void shouldProcessEventSuccessfully() throws Exception {
		UUID eventId = UUID.randomUUID();
		TransactionPendingEvent event = new TransactionPendingEvent(eventId, UUID.randomUUID().toString(),
				Instant.now(), "SRC123", "TGT456", new BigDecimal("100.00"), TransactionRecord.TransactionType.TRANSFER,
				"user1");
		String payload = "{}";

		when(objectMapper.readValue(payload, TransactionPendingEvent.class)).thenReturn(event);

		listener.onTransactionEvent(payload, EventType.TRANSACTION_PENDING.name());

		verify(processTransactionUseCase, times(1)).process(any(TransactionPendingEvent.class));
	}

	@Test
	void shouldIgnoreNonPendingEvents() throws Exception {
		String payload = "{}";

		listener.onTransactionEvent(payload, EventType.TRANSACTION_COMPLETED.name());

		verify(processTransactionUseCase, never()).process(any());
	}

	@Test
	void shouldThrowRuntimeExceptionOnProcessingFailure() throws Exception {
		String payload = "invalid-json";

		when(objectMapper.readValue(payload, TransactionPendingEvent.class))
				.thenThrow(new RuntimeException("JSON parse error"));

		RuntimeException exception = org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
				() -> listener.onTransactionEvent(payload, EventType.TRANSACTION_PENDING.name()));

		org.junit.jupiter.api.Assertions.assertEquals("Kafka message processing failed", exception.getMessage());
		org.junit.jupiter.api.Assertions.assertEquals("JSON parse error", exception.getCause().getMessage());

		verify(processTransactionUseCase, never()).process(any());
	}
}
