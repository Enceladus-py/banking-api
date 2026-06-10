package com.fintech.banking.coreapi.transaction.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.fintech.banking.coreapi.transaction.domain.model.TransactionRecord.TransactionType;

/**
 * Domain event published when a transaction enters a pending state.
 *
 * @param eventId
 *            the unique ID of this event instance
 * @param transactionId
 *            the unique ID of the pending transaction
 * @param timestamp
 *            the instant when this event occurred
 * @param sourceAccountNumber
 *            the source account number (if applicable)
 * @param targetAccountNumber
 *            the target account number (if applicable)
 * @param amount
 *            the transaction amount
 * @param type
 *            the transaction type
 * @param requesterId
 *            the user ID requesting the transaction
 */
public record TransactionPendingEvent(UUID eventId, String transactionId, Instant timestamp, String sourceAccountNumber,
		String targetAccountNumber, BigDecimal amount, TransactionType type,
		String requesterId) implements TransactionEvent {

	/**
	 * Returns the event type for this event, which is TRANSACTION_PENDING.
	 *
	 * @return the event type
	 */
	@Override
	public EventType eventType() {
		return EventType.TRANSACTION_PENDING;
	}
}
