package com.fintech.banking.coreapi.transaction.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import com.fintech.banking.coreapi.transaction.domain.event.EventType;
import com.fintech.banking.coreapi.transaction.domain.model.AggregateType;

import jakarta.persistence.*;
import lombok.*;

/**
 * JPA entity representing an event in the outbox table.
 */
@Entity
@Table(name = "outbox_events", schema = "events_schema")
@Getter
@AllArgsConstructor
@Builder
public class OutboxEventJpaEntity {

	/**
	 * Default constructor required by JPA.
	 */
	public OutboxEventJpaEntity() {
	}

	@Id
	private UUID id;

	@Enumerated(EnumType.STRING)
	@Column(name = "aggregate_type", nullable = false)
	private AggregateType aggregateType;

	@Column(name = "aggregate_id", nullable = false)
	private String aggregateId;

	@Enumerated(EnumType.STRING)
	@Column(name = "event_type", nullable = false)
	private EventType eventType;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String payload;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Setter // Only status is mutable after creation
	private OutboxStatus status;

	@Column(name = "retry_count", nullable = false)
	@Setter // Incremented by the scheduler on each failed attempt
	private int retryCount;
}
