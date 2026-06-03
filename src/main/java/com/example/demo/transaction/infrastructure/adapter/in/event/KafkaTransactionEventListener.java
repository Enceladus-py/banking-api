package com.example.demo.transaction.infrastructure.adapter.in.event;

import java.time.LocalDateTime;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.transaction.application.port.in.ProcessTransactionUseCase;
import com.example.demo.transaction.domain.event.EventType;
import com.example.demo.transaction.domain.event.TransactionPendingEvent;
import com.example.demo.transaction.infrastructure.adapter.out.persistence.entity.ProcessedEventJpaEntity;
import com.example.demo.transaction.infrastructure.adapter.out.persistence.repository.SpringDataProcessedEventRepository;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

/**
 * Kafka listener for processing transaction events.
 */
@Component
@Slf4j
public class KafkaTransactionEventListener {

	private final ProcessTransactionUseCase processTransactionUseCase;
	private final SpringDataProcessedEventRepository processedEventRepository;
	private final ObjectMapper objectMapper;

	/**
	 * Constructs a new KafkaTransactionEventListener with the specified services.
	 *
	 * @param processTransactionUseCase
	 *            the use case for processing transactions
	 * @param processedEventRepository
	 *            the repository for tracking processed events
	 * @param objectMapper
	 *            the mapper for deserializing payloads
	 */
	public KafkaTransactionEventListener(ProcessTransactionUseCase processTransactionUseCase,
			SpringDataProcessedEventRepository processedEventRepository, ObjectMapper objectMapper) {
		this.processTransactionUseCase = processTransactionUseCase;
		this.processedEventRepository = processedEventRepository;
		this.objectMapper = objectMapper;
	}

	/**
	 * Kafka listener method triggered when a message is received on
	 * "transaction-events" topic.
	 *
	 * @param payload
	 *            the JSON string payload of the event
	 * @param eventType
	 *            the type of event sent in message header
	 */
	@KafkaListener(topics = "transaction-events", groupId = "transaction-group")
	@Transactional
	public void onTransactionEvent(@org.springframework.messaging.handler.annotation.Payload String payload,
			@org.springframework.messaging.handler.annotation.Header("eventType") String eventType) {
		try {
			if (!EventType.TRANSACTION_PENDING.name().equals(eventType)) {
				log.debug("Ignoring non-pending transaction event type: {}", eventType);
				return;
			}

			TransactionPendingEvent pendingEvent = objectMapper.readValue(payload, TransactionPendingEvent.class);

			log.info("Received transaction pending event from Kafka: {} for transaction: {}", pendingEvent.eventId(),
					pendingEvent.transactionId());

			// Idempotency: attempt to insert a processed_events row first.
			// If the row already exists, the UNIQUE constraint on 'id' raises a
			// DataIntegrityViolationException — we catch it and skip processing.
			try {
				processedEventRepository
						.saveAndFlush(new ProcessedEventJpaEntity(pendingEvent.eventId(), LocalDateTime.now()));
			} catch (DataIntegrityViolationException e) {
				log.warn("Event {} has already been processed. Ignoring duplicate.", pendingEvent.eventId());
				return;
			}

			processTransactionUseCase.process(pendingEvent);
		} catch (Exception e) {
			log.error("Failed to process Kafka message: {}", e.getMessage(), e);
			throw new RuntimeException("Kafka message processing failed", e);
		}
	}
}
