package com.fintech.banking.coreapi.account.application.port.in;

import com.fintech.banking.coreapi.account.domain.model.Account;

/**
 * Inbound port interface for creating a new bank account.
 */
public interface CreateAccountUseCase {

	/**
	 * Creates a new account according to the given command.
	 *
	 * @param command
	 *            the command specifying request details
	 * @return the created account
	 */
	Account createAccount(CreateAccountCommand command);

	/**
	 * Command containing input details for creating an account.
	 *
	 * @param requesterId
	 *            the user ID of the requesting owner
	 */
	record CreateAccountCommand(String requesterId) {
		/**
		 * Validates that requester ID is provided and not empty.
		 *
		 * @param requesterId
		 *            the user ID of the requester
		 */
		public CreateAccountCommand {
			java.util.Objects.requireNonNull(requesterId, "Requester ID is required");
			if (requesterId.trim().isBlank()) {
				throw new IllegalArgumentException("Requester ID cannot be blank");
			}
		}
	}
}
