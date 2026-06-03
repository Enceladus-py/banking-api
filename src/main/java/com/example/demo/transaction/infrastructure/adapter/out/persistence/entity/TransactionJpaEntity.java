package com.example.demo.transaction.infrastructure.adapter.out.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.example.demo.transaction.domain.model.TransactionRecord.TransactionStatus;
import com.example.demo.transaction.domain.model.TransactionRecord.TransactionType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "transaction_records")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionJpaEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "source_account_number")
	private String sourceAccountNumber;

	@Column(name = "target_account_number")
	private String targetAccountNumber;

	@Column(nullable = false)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TransactionType type;

	@Column(nullable = false)
	private LocalDateTime timestamp;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TransactionStatus status;

	@Column(name = "failure_reason")
	private String failureReason;

	/**
	 * Tracks whether this entity instance has been persisted yet. Starts as
	 * {@code true} for newly-constructed instances (INSERT path). Flipped to
	 * {@code false} by {@link #onPersist()} and {@link #onLoad()} so that
	 * subsequent {@code save()} calls take the UPDATE path without issuing an extra
	 * SELECT.
	 */
	@Transient
	@Builder.Default
	private boolean isNew = true;

	@PostPersist
	@PostLoad
	void onPersist() {
		this.isNew = false;
	}

	@Override
	public boolean isNew() {
		return isNew;
	}
}
