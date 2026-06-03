package com.example.demo.transaction.domain.event;

import java.time.Instant;
import java.util.UUID;

import com.example.demo.transaction.domain.model.TransactionRecord.TransactionType;

/**
 * Sealed hierarchy of domain events emitted during a transaction lifecycle.
 * <p>
 * {@link TransactionType} is imported from the domain model so there is a
 * single canonical definition shared by both events and records.
 */
public sealed interface TransactionEvent
		permits TransactionPendingEvent, TransactionCompletedEvent, TransactionFailedEvent {

	/**
	 * Returns the unique ID of the event instance.
	 *
	 * @return the event ID
	 */
	UUID eventId();

	/**
	 * Returns the unique ID of the associated transaction.
	 *
	 * @return the transaction ID
	 */
	String transactionId();

	/**
	 * Returns the instant when the event occurred.
	 *
	 * @return the event timestamp
	 */
	Instant timestamp();

	/**
	 * Returns the event type of this domain event.
	 *
	 * @return the event type
	 */
	EventType eventType();
}
