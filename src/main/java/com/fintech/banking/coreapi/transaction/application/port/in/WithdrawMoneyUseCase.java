package com.fintech.banking.coreapi.transaction.application.port.in;

import java.math.BigDecimal;

import com.fintech.banking.coreapi.transaction.domain.model.TransactionRecord;

/**
 * Inbound port interface for withdrawing money from a bank account.
 */
public interface WithdrawMoneyUseCase {

	/**
	 * Processes a withdrawal from the account specified in the command.
	 *
	 * @param command
	 *            the withdrawal command details
	 * @return the transaction record for the withdrawal
	 */
	TransactionRecord withdraw(WithdrawCommand command);

	/**
	 * Command containing input details for withdrawing money.
	 *
	 * @param accountId
	 *            the account number to withdraw from
	 * @param amount
	 *            the withdrawal amount
	 * @param requesterId
	 *            the user ID requesting the withdrawal
	 */
	record WithdrawCommand(String accountId, BigDecimal amount, String requesterId) {
		/**
		 * Constructor validating withdrawal input fields.
		 *
		 * @param accountId
		 *            the account number
		 * @param amount
		 *            the withdrawal amount
		 * @param requesterId
		 *            the requester user ID
		 */
		public WithdrawCommand {
			java.util.Objects.requireNonNull(accountId, "Account ID is required");
			if (accountId.trim().isBlank()) {
				throw new IllegalArgumentException("Account ID cannot be blank");
			}
			java.util.Objects.requireNonNull(requesterId, "Requester ID is required");
			if (requesterId.trim().isBlank()) {
				throw new IllegalArgumentException("Requester ID cannot be blank");
			}
			if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
				throw new IllegalArgumentException("Withdrawal amount must be strictly positive");
			}
		}
	}
}
