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
import org.springframework.dao.DataIntegrityViolationException;

import com.example.demo.transaction.application.port.in.ProcessTransactionUseCase;
import com.example.demo.transaction.domain.event.TransactionPendingEvent;
import com.example.demo.transaction.domain.model.TransactionRecord;
import com.example.demo.transaction.infrastructure.adapter.out.persistence.entity.ProcessedEventJpaEntity;
import com.example.demo.transaction.infrastructure.adapter.out.persistence.repository.SpringDataProcessedEventRepository;

import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class KafkaTransactionEventListenerTest {

	@Mock
	private ProcessTransactionUseCase processTransactionUseCase;

	@Mock
	private SpringDataProcessedEventRepository processedEventRepository;

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

		// When saving the processed event succeeds
		when(processedEventRepository.saveAndFlush(any(ProcessedEventJpaEntity.class)))
				.thenReturn(new ProcessedEventJpaEntity(eventId, java.time.LocalDateTime.now()));

		listener.onTransactionEvent(payload, "TransactionPendingEvent");

		verify(processedEventRepository, times(1)).saveAndFlush(any(ProcessedEventJpaEntity.class));
		verify(processTransactionUseCase, times(1)).process(any(TransactionPendingEvent.class));
	}

	@Test
	void shouldIgnoreDuplicateEventWhenDataIntegrityViolationExceptionIsThrown() throws Exception {
		UUID eventId = UUID.randomUUID();
		TransactionPendingEvent event = new TransactionPendingEvent(eventId, UUID.randomUUID().toString(),
				Instant.now(), "SRC123", "TGT456", new BigDecimal("100.00"), TransactionRecord.TransactionType.TRANSFER,
				"user1");
		String payload = "{}";

		when(objectMapper.readValue(payload, TransactionPendingEvent.class)).thenReturn(event);

		// When saving throws DataIntegrityViolationException (duplicate event)
		when(processedEventRepository.saveAndFlush(any(ProcessedEventJpaEntity.class)))
				.thenThrow(new DataIntegrityViolationException("Duplicate key violation"));

		listener.onTransactionEvent(payload, "TransactionPendingEvent");

		verify(processedEventRepository, times(1)).saveAndFlush(any(ProcessedEventJpaEntity.class));
		// processTransactionUseCase.process should never be called
		verify(processTransactionUseCase, never()).process(any());
	}

	@Test
	void shouldIgnoreNonPendingEvents() throws Exception {
		String payload = "{}";

		listener.onTransactionEvent(payload, "TransactionCompletedEvent");

		verify(processedEventRepository, never()).saveAndFlush(any(ProcessedEventJpaEntity.class));
		verify(processTransactionUseCase, never()).process(any());
	}
}
