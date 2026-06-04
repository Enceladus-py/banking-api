package com.example.demo.transaction.domain.event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class TransactionEventTest {

	@Test
	void shouldCreateTransactionFailedEvent() {
		UUID eventId = UUID.randomUUID();
		String txId = "tx-123";
		Instant now = Instant.now();
		String reason = "Insufficient funds";

		TransactionFailedEvent event = new TransactionFailedEvent(eventId, txId, now, reason);

		assertEquals(eventId, event.eventId());
		assertEquals(txId, event.transactionId());
		assertEquals(now, event.timestamp());
		assertEquals(reason, event.failureReason());
		assertEquals(EventType.TRANSACTION_FAILED, event.eventType());
	}

	@Test
	void shouldCreateTransactionCompletedEvent() {
		UUID eventId = UUID.randomUUID();
		String txId = "tx-456";
		Instant now = Instant.now();

		TransactionCompletedEvent event = new TransactionCompletedEvent(eventId, txId, now);

		assertEquals(eventId, event.eventId());
		assertEquals(txId, event.transactionId());
		assertEquals(now, event.timestamp());
		assertEquals(EventType.TRANSACTION_COMPLETED, event.eventType());
	}
}
