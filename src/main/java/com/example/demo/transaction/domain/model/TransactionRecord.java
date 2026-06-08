package com.example.demo.transaction.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Getter;

/**
 * Domain model representing a transaction record (deposit, withdrawal, or
 * transfer).
 */
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

	/**
	 * Enumeration of transaction types.
	 */
	public enum TransactionType {
		/**
		 * Deposit transaction.
		 */
		DEPOSIT,
		/**
		 * Withdrawal transaction.
		 */
		WITHDRAWAL,
		/**
		 * Transfer transaction.
		 */
		TRANSFER
	}

	/**
	 * Enumeration of transaction processing statuses.
	 */
	public enum TransactionStatus {
		/**
		 * Transaction is pending processing.
		 */
		PENDING,
		/**
		 * Transaction completed successfully.
		 */
		COMPLETED,
		/**
		 * Transaction failed.
		 */
		FAILED
	}

	/**
	 * Factory method for creating a new pending transaction with validations.
	 *
	 * @param sourceAccountNumber
	 *            the source account number
	 * @param targetAccountNumber
	 *            the target account number
	 * @param amount
	 *            the transaction amount
	 * @param type
	 *            the transaction type
	 * @return the newly created pending TransactionRecord
	 */
	public static TransactionRecord createNew(String sourceAccountNumber, String targetAccountNumber, BigDecimal amount,
			TransactionType type) {
		TransactionRecord record = new TransactionRecord(UUID.randomUUID().toString(), sourceAccountNumber,
				targetAccountNumber, amount, type, LocalDateTime.now(), TransactionStatus.PENDING, null);
		validate(sourceAccountNumber, targetAccountNumber, amount, type);
		return record;
	}

	/**
	 * Reconstitutes a TransactionRecord from persistent storage.
	 *
	 * @param id
	 *            the transaction ID
	 * @param sourceAccountNumber
	 *            the source account number
	 * @param targetAccountNumber
	 *            the target account number
	 * @param amount
	 *            the transaction amount
	 * @param type
	 *            the transaction type
	 * @param timestamp
	 *            the transaction timestamp
	 * @param status
	 *            the transaction status
	 * @param failureReason
	 *            the reason why the transaction failed, if any
	 * @return the reconstituted TransactionRecord
	 */
	public static TransactionRecord reconstitute(String id, String sourceAccountNumber, String targetAccountNumber,
			BigDecimal amount, TransactionType type, LocalDateTime timestamp, TransactionStatus status,
			String failureReason) {
		return new TransactionRecord(id, sourceAccountNumber, targetAccountNumber, amount, type, timestamp, status,
				failureReason);
	}

	/**
	 * Private constructor used by static factory methods.
	 *
	 * @param id
	 *            the transaction ID
	 * @param sourceAccountNumber
	 *            the source account number
	 * @param targetAccountNumber
	 *            the target account number
	 * @param amount
	 *            the transaction amount
	 * @param type
	 *            the transaction type
	 * @param timestamp
	 *            the transaction timestamp
	 * @param status
	 *            the transaction status
	 * @param failureReason
	 *            the reason why the transaction failed, if any
	 */
	private TransactionRecord(String id, String sourceAccountNumber, String targetAccountNumber, BigDecimal amount,
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
	 * Returns a new TransactionRecord representing this transaction in the
	 * COMPLETED state.
	 *
	 * @return the completed transaction record
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
	 * Returns a new TransactionRecord representing this transaction in the FAILED
	 * state with a human-readable reason.
	 *
	 * @param reason
	 *            the failure reason
	 * @return the failed transaction record
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
