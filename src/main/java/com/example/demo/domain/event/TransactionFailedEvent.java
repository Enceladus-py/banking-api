package com.example.demo.domain.event;

import java.time.Instant;
import java.util.UUID;

public record TransactionFailedEvent(
        UUID eventId,
        String transactionId,
        Instant timestamp,
        String failureReason
) implements TransactionEvent {}
