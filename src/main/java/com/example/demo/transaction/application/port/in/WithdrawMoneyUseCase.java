package com.example.demo.transaction.application.port.in;

import java.math.BigDecimal;

import com.example.demo.transaction.domain.model.TransactionRecord;

public interface WithdrawMoneyUseCase {

	TransactionRecord withdraw(WithdrawCommand command);

	record WithdrawCommand(String accountId, BigDecimal amount, String requesterId) {
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
