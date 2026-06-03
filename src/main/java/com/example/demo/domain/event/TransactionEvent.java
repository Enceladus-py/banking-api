package com.example.demo.domain.event;

import java.time.Instant;
import java.util.UUID;

import com.example.demo.domain.model.TransactionRecord.TransactionType;

/**
 * Sealed hierarchy of domain events emitted during a transaction lifecycle.
 * <p>
 * {@link TransactionType} is imported from the domain model so there is a
 * single canonical definition shared by both events and records.
 */
public sealed interface TransactionEvent
		permits TransactionPendingEvent, TransactionCompletedEvent, TransactionFailedEvent {

	UUID eventId();
	String transactionId();
	Instant timestamp();
}
