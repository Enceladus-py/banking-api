package com.example.demo.transaction.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record TransferRequest(
		@NotNull(message = "Source account number is required") @Size(min = 10, max = 10, message = "Source account number must be exactly 10 characters") String sourceAccountNumber,

		@NotNull(message = "Target account number is required") @Size(min = 10, max = 10, message = "Target account number must be exactly 10 characters") String targetAccountNumber,

		@NotNull(message = "Transfer amount is required") @Positive(message = "Transfer amount must be greater than zero") BigDecimal amount) {
}
