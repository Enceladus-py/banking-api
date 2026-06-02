package com.example.demo.domain.event;

import java.time.Instant;
import java.util.UUID;

public sealed interface TransactionEvent permits
        TransactionPendingEvent,
        TransactionCompletedEvent,
        TransactionFailedEvent {

    UUID eventId();
    String transactionId();
    Instant timestamp();

    enum TransactionType {
        DEPOSIT,
        WITHDRAWAL,
        TRANSFER
    }
}
