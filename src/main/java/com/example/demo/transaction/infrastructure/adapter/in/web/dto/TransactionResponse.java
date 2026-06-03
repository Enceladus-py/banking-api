package com.example.demo.transaction.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.demo.transaction.domain.model.TransactionRecord;
import com.example.demo.transaction.domain.model.TransactionRecord.TransactionStatus;
import com.example.demo.transaction.domain.model.TransactionRecord.TransactionType;

public record TransactionResponse(String id, String sourceAccountNumber, String targetAccountNumber, BigDecimal amount,
		TransactionType type, LocalDateTime timestamp, TransactionStatus status, String failureReason) {
	public static TransactionResponse from(TransactionRecord record) {
		return new TransactionResponse(record.getId(), record.getSourceAccountNumber(), record.getTargetAccountNumber(),
				record.getAmount(), record.getType(), record.getTimestamp(), record.getStatus(),
				record.getFailureReason());
	}
}
