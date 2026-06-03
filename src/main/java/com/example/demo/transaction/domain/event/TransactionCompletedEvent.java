package com.example.demo.transaction.domain.event;

import java.time.Instant;
import java.util.UUID;

public record TransactionCompletedEvent(UUID eventId, String transactionId,
		Instant timestamp) implements TransactionEvent {

	@Override
	public EventType eventType() {
		return EventType.TRANSACTION_COMPLETED;
	}
}
