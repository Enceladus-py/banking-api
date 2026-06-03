package com.example.demo.transaction.application.port.in;

import java.math.BigDecimal;

import com.example.demo.transaction.domain.model.TransactionRecord;

/**
 * Inbound port interface for transferring money between bank accounts.
 */
public interface TransferMoneyUseCase {

	/**
	 * Processes a transfer transaction between source and target accounts.
	 *
	 * @param command
	 *            the transfer command details
	 * @return the transaction record for the transfer
	 */
	TransactionRecord transfer(TransferCommand command);

	/**
	 * Command containing input details for transferring money.
	 *
	 * @param sourceAccountNumber
	 *            the source account number
	 * @param targetAccountNumber
	 *            the target account number
	 * @param amount
	 *            the transfer amount
	 * @param requesterId
	 *            the user ID requesting the transfer
	 */
	record TransferCommand(String sourceAccountNumber, String targetAccountNumber, BigDecimal amount,
			String requesterId) {
		/**
		 * Constructor validating transfer input fields.
		 *
		 * @param sourceAccountNumber
		 *            the source account number
		 * @param targetAccountNumber
		 *            the target account number
		 * @param amount
		 *            the transfer amount
		 * @param requesterId
		 *            the requester user ID
		 */
		public TransferCommand {
			java.util.Objects.requireNonNull(sourceAccountNumber, "Source account number is required");
			if (sourceAccountNumber.trim().isBlank()) {
				throw new IllegalArgumentException("Source account number cannot be blank");
			}
			java.util.Objects.requireNonNull(targetAccountNumber, "Target account number is required");
			if (targetAccountNumber.trim().isBlank()) {
				throw new IllegalArgumentException("Target account number cannot be blank");
			}
			java.util.Objects.requireNonNull(requesterId, "Requester ID is required");
			if (requesterId.trim().isBlank()) {
				throw new IllegalArgumentException("Requester ID cannot be blank");
			}
			if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
				throw new IllegalArgumentException("Transfer amount must be strictly positive");
			}
			if (sourceAccountNumber.equals(targetAccountNumber)) {
				throw new IllegalArgumentException("Cannot transfer to the same account");
			}
		}
	}
}
