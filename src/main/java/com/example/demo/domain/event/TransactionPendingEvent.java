package com.example.demo.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionPendingEvent(
        UUID eventId,
        String transactionId,
        Instant timestamp,
        String sourceAccountNumber,
        String targetAccountNumber,
        BigDecimal amount,
        TransactionEvent.TransactionType type,
        String requesterId
) implements TransactionEvent {}
