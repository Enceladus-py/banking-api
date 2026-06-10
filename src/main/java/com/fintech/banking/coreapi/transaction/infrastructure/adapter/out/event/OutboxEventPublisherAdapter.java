package com.fintech.banking.coreapi.transaction.infrastructure.adapter.out.event;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.fintech.banking.coreapi.transaction.application.port.out.EventPublisher;
import com.fintech.banking.coreapi.transaction.domain.event.TransactionEvent;
import com.fintech.banking.coreapi.transaction.domain.model.AggregateType;
import com.fintech.banking.coreapi.transaction.infrastructure.adapter.out.persistence.entity.OutboxEventJpaEntity;
import com.fintech.banking.coreapi.transaction.infrastructure.adapter.out.persistence.entity.OutboxStatus;
import com.fintech.banking.coreapi.transaction.infrastructure.adapter.out.persistence.repository.SpringDataOutboxEventRepository;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Outbox implementation of the EventPublisher port.
 */
@Component
public class OutboxEventPublisherAdapter implements EventPublisher {

	private final SpringDataOutboxEventRepository outboxRepository;
	private final ObjectMapper objectMapper;

	/**
	 * Constructs a new OutboxEventPublisherAdapter with the specified dependencies.
	 *
	 * @param outboxRepository
	 *            the Spring Data repository for outbox events
	 * @param objectMapper
	 *            the mapper for serializing events
	 */
	public OutboxEventPublisherAdapter(SpringDataOutboxEventRepository outboxRepository, ObjectMapper objectMapper) {
		this.outboxRepository = outboxRepository;
		this.objectMapper = objectMapper;
	}

	@Override
	public void publish(TransactionEvent event) {
		try {
			String payload = objectMapper.writeValueAsString(event);
			OutboxEventJpaEntity entity = OutboxEventJpaEntity.builder().id(event.eventId())
					.aggregateType(AggregateType.TRANSACTION).aggregateId(event.transactionId())
					.eventType(event.eventType()).payload(payload).createdAt(Instant.now()).status(OutboxStatus.PENDING)
					.build();
			outboxRepository.save(entity);
		} catch (JacksonException e) {
			throw new RuntimeException("Failed to serialize event: " + event.eventId(), e);
		}
	}
}
