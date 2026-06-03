package com.example.demo.domain.event;

import java.time.Instant;
import java.util.UUID;

public record TransactionCompletedEvent(UUID eventId, String transactionId,
		Instant timestamp) implements TransactionEvent {
}
