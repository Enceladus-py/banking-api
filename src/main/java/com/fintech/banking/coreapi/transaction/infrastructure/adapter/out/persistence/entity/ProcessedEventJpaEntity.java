package com.fintech.banking.coreapi.transaction.infrastructure.adapter.out.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.*;

/**
 * JPA entity representing a processed domain event for idempotency checks.
 */
@Entity
@Table(name = "processed_events", schema = "events_schema")
@Getter
@Setter
@AllArgsConstructor
@Builder
public class ProcessedEventJpaEntity {

	/**
	 * Default constructor required by JPA.
	 */
	public ProcessedEventJpaEntity() {
	}

	@Id
	private UUID id; // Event ID

	@Column(name = "processed_at", nullable = false)
	private Instant processedAt;
}
