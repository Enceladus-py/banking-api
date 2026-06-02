package com.example.demo.infrastructure.adapter.in.web.dto;

import com.example.demo.domain.model.TransactionRecord;
import com.example.demo.domain.model.TransactionRecord.TransactionStatus;
import com.example.demo.domain.model.TransactionRecord.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
    String id,
    String sourceAccountNumber,
    String targetAccountNumber,
    BigDecimal amount,
    TransactionType type,
    LocalDateTime timestamp,
    TransactionStatus status,
    String failureReason
) {
    public static TransactionResponse from(TransactionRecord record) {
        return new TransactionResponse(
            record.getId(),
            record.getSourceAccountNumber(),
            record.getTargetAccountNumber(),
            record.getAmount(),
            record.getType(),
            record.getTimestamp(),
            record.getStatus(),
            record.getFailureReason()
        );
    }
}
