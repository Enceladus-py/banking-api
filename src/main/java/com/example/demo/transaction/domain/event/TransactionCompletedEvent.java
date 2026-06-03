package com.example.demo.transaction.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain event published when a transaction completes successfully.
 *
 * @param eventId
 *            the unique ID of this event instance
 * @param transactionId
 *            the unique ID of the completed transaction
 * @param timestamp
 *            the instant when this event occurred
 */
public record TransactionCompletedEvent(UUID eventId, String transactionId,
		Instant timestamp) implements TransactionEvent {

	/**
	 * Returns the event type for this event, which is TRANSACTION_COMPLETED.
	 *
	 * @return the event type
	 */
	@Override
	public EventType eventType() {
		return EventType.TRANSACTION_COMPLETED;
	}
}
