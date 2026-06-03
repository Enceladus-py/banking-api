package com.example.demo.transaction.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when a transaction fails.
 *
 * @param eventId
 *            the unique ID of this event instance
 * @param transactionId
 *            the unique ID of the failed transaction
 * @param timestamp
 *            the instant when this event occurred
 * @param failureReason
 *            the reason describing why the transaction failed
 */
public record TransactionFailedEvent(UUID eventId, String transactionId, Instant timestamp,
		String failureReason) implements TransactionEvent {

	/**
	 * Returns the event type for this event, which is TRANSACTION_FAILED.
	 *
	 * @return the event type
	 */
	@Override
	public EventType eventType() {
		return EventType.TRANSACTION_FAILED;
	}
}
