package com.example.demo.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Getter;

@Getter
public class TransactionRecord {

	private final String id;
	private final String sourceAccountNumber; // Can be null for direct deposits
	private final String targetAccountNumber; // Can be null for direct withdrawals
	private final BigDecimal amount;
	private final TransactionType type;
	private final LocalDateTime timestamp;
	private final TransactionStatus status;
	private final String failureReason;

	public enum TransactionType {
		DEPOSIT, WITHDRAWAL, TRANSFER
	}

	public enum TransactionStatus {
		PENDING, COMPLETED, FAILED
	}

	// Constructor for creating a BRAND NEW transaction (Business Layer) - default
	// to PENDING
	public TransactionRecord(String sourceAccountNumber, String targetAccountNumber, BigDecimal amount,
			TransactionType type) {
		this(UUID.randomUUID().toString(), sourceAccountNumber, targetAccountNumber, amount, type, LocalDateTime.now(),
				TransactionStatus.PENDING, null);
		validate(sourceAccountNumber, targetAccountNumber, amount, type);
	}

	// Lenient Constructor for database re-hydration and mapping
	public TransactionRecord(String id, String sourceAccountNumber, String targetAccountNumber, BigDecimal amount,
			TransactionType type, LocalDateTime timestamp, TransactionStatus status, String failureReason) {
		this.id = id;
		this.sourceAccountNumber = sourceAccountNumber;
		this.targetAccountNumber = targetAccountNumber;
		this.amount = amount;
		this.type = type;
		this.timestamp = timestamp;
		this.status = status;
		this.failureReason = failureReason;
	}

	/**
	 * Returns a new {@link TransactionRecord} representing this transaction in the
	 * {@link TransactionStatus#COMPLETED} state. The original record is not mutated
	 * (immutable wither pattern).
	 */
	public TransactionRecord complete() {
		if (this.status != TransactionStatus.PENDING) {
			throw new IllegalStateException(
					"Only PENDING transactions can be completed, but current status is " + this.status);
		}
		return new TransactionRecord(id, sourceAccountNumber, targetAccountNumber, amount, type, timestamp,
				TransactionStatus.COMPLETED, null);
	}

	/**
	 * Returns a new {@link TransactionRecord} representing this transaction in the
	 * {@link TransactionStatus#FAILED} state with a human-readable reason. The
	 * original record is not mutated.
	 */
	public TransactionRecord fail(String reason) {
		if (this.status != TransactionStatus.PENDING) {
			throw new IllegalStateException(
					"Only PENDING transactions can be failed, but current status is " + this.status);
		}
		return new TransactionRecord(id, sourceAccountNumber, targetAccountNumber, amount, type, timestamp,
				TransactionStatus.FAILED, reason);
	}

	private static void validate(String sourceAccountNumber, String targetAccountNumber, BigDecimal amount,
			TransactionType type) {
		// 1. Amount validations
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Transaction amount must be strictly positive");
		}

		// 2. Type validation
		if (type == null) {
			throw new IllegalArgumentException("Transaction type cannot be null");
		}

		// 3. Account participation validations
		if (sourceAccountNumber == null && targetAccountNumber == null) {
			throw new IllegalArgumentException("Source and target accounts cannot both be null");
		}

		// 4. Specific rules for types
		if (type == TransactionType.DEPOSIT) {
			if (sourceAccountNumber != null)
				throw new IllegalArgumentException("Deposits cannot have a source account");
		} else if (type == TransactionType.WITHDRAWAL) {
			if (sourceAccountNumber == null)
				throw new IllegalArgumentException("Withdrawals must specify a source account");
			if (targetAccountNumber != null)
				throw new IllegalArgumentException("Withdrawals cannot have a target account");
		} else {
			if (sourceAccountNumber == null || targetAccountNumber == null) {
				throw new IllegalArgumentException("Transfers must specify both source and target accounts");
			}
			if (sourceAccountNumber.equals(targetAccountNumber)) {
				throw new IllegalArgumentException("Cannot transfer money to the same account");
			}
		}
	}

}
