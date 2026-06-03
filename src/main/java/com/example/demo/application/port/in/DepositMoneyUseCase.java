package com.example.demo.application.port.in;

import java.math.BigDecimal;

import com.example.demo.domain.model.TransactionRecord;

public interface DepositMoneyUseCase {

	TransactionRecord deposit(DepositCommand command);

	record DepositCommand(String accountId, BigDecimal amount, String requesterId) {
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
