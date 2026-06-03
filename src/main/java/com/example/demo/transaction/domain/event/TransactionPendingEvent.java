package com.example.demo.transaction.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.example.demo.transaction.domain.model.TransactionRecord.TransactionType;

public record TransactionPendingEvent(UUID eventId, String transactionId, Instant timestamp, String sourceAccountNumber,
		String targetAccountNumber, BigDecimal amount, TransactionType type,
		String requesterId) implements TransactionEvent {

	@Override
	public EventType eventType() {
		return EventType.TRANSACTION_PENDING;
	}
}
