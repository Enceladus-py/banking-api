package com.example.demo.account.application.port.in;

import com.example.demo.account.domain.model.Account;

public interface CreateAccountUseCase {

	Account createAccount(CreateAccountCommand command);

	// Immutable record carrying the exact data needed for this use case
	record CreateAccountCommand(String requesterId) {
		public CreateAccountCommand {
			java.util.Objects.requireNonNull(requesterId, "Requester ID is required");
			if (requesterId.trim().isBlank()) {
				throw new IllegalArgumentException("Requester ID cannot be blank");
			}
		}
	}
}
