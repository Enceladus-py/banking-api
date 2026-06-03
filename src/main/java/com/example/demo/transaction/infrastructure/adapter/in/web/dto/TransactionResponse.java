package com.example.demo.transaction.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.demo.transaction.domain.model.TransactionRecord;
import com.example.demo.transaction.domain.model.TransactionRecord.TransactionStatus;
import com.example.demo.transaction.domain.model.TransactionRecord.TransactionType;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response payload representing a transaction status or receipt.
 *
 * @param id
 *            the transaction ID
 * @param sourceAccountNumber
 *            the source account number (for deposits and transfers)
 * @param targetAccountNumber
 *            the target account number (for withdrawals and transfers)
 * @param amount
 *            the transaction amount
 * @param type
 *            the transaction type (DEPOSIT, WITHDRAWAL, TRANSFER)
 * @param timestamp
 *            the transaction timestamp
 * @param status
 *            the transaction status (PENDING, COMPLETED, FAILED)
 * @param failureReason
 *            the reason for failure, if any
 */
@Schema(description = "Response payload containing transaction record details")
public record TransactionResponse(
		@Schema(description = "The unique identifier of the transaction", example = "123e4567-e89b-12d3-a456-426614174000") String id,
		@Schema(description = "The 10-digit source account number", example = "1234567890") String sourceAccountNumber,
		@Schema(description = "The 10-digit target account number", example = "0987654321") String targetAccountNumber,
		@Schema(description = "The transaction amount", example = "100.00") BigDecimal amount,
		@Schema(description = "The transaction type") TransactionType type,
		@Schema(description = "The timestamp when the transaction occurred") LocalDateTime timestamp,
		@Schema(description = "The current status of the transaction") TransactionStatus status,
		@Schema(description = "The failure reason, if the transaction failed") String failureReason) {

	/**
	 * Creates a TransactionResponse from a TransactionRecord domain object.
	 *
	 * @param record
	 *            the transaction record domain model
	 * @return the TransactionResponse DTO
	 */
	public static TransactionResponse from(TransactionRecord record) {
		return new TransactionResponse(record.getId(), record.getSourceAccountNumber(), record.getTargetAccountNumber(),
				record.getAmount(), record.getType(), record.getTimestamp(), record.getStatus(),
				record.getFailureReason());
	}
}
