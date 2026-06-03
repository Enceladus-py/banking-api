package com.example.demo.transaction.application.port.in;

import java.math.BigDecimal;

import com.example.demo.transaction.domain.model.TransactionRecord;

/**
 * Inbound port interface for depositing money into a bank account.
 */
public interface DepositMoneyUseCase {

	/**
	 * Processes a deposit into the account specified in the command.
	 *
	 * @param command
	 *            the deposit command details
	 * @return the transaction record for the deposit
	 */
	TransactionRecord deposit(DepositCommand command);

	/**
	 * Command containing input details for depositing money.
	 *
	 * @param accountId
	 *            the account number to receive the deposit
	 * @param amount
	 *            the deposit amount
	 * @param requesterId
	 *            the user ID requesting the deposit
	 */
	record DepositCommand(String accountId, BigDecimal amount, String requesterId) {
		/**
		 * Constructor validating deposit input fields.
		 *
		 * @param accountId
		 *            the account number
		 * @param amount
		 *            the deposit amount
		 * @param requesterId
		 *            the requester user ID
		 */
		public DepositCommand {
			java.util.Objects.requireNonNull(accountId, "Account ID is required");
			if (accountId.trim().isBlank()) {
				throw new IllegalArgumentException("Account ID cannot be blank");
			}
			java.util.Objects.requireNonNull(requesterId, "Requester ID is required");
			if (requesterId.trim().isBlank()) {
				throw new IllegalArgumentException("Requester ID cannot be blank");
			}
			if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
				throw new IllegalArgumentException("Deposit amount must be strictly positive");
			}
		}
	}
}
