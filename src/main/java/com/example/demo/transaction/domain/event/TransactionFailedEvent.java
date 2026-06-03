package com.example.demo.transaction.domain.event;

import java.time.Instant;
import java.util.UUID;

public record TransactionFailedEvent(UUID eventId, String transactionId, Instant timestamp,
		String failureReason) implements TransactionEvent {

	@Override
	public EventType eventType() {
		return EventType.TRANSACTION_FAILED;
	}
}
