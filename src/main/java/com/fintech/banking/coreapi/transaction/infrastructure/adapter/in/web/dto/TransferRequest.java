package com.fintech.banking.coreapi.transaction.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request payload to transfer money between accounts.
 *
 * @param sourceAccountNumber
 *            the source account number
 * @param targetAccountNumber
 *            the target account number
 * @param amount
 *            the transfer amount
 */
@Schema(description = "Request payload to transfer money between accounts")
public record TransferRequest(
		@NotNull(message = "Source account number is required") @Size(min = 10, max = 10, message = "Source account number must be exactly 10 characters") @Schema(description = "The 10-digit source account number", example = "1234567890") String sourceAccountNumber,

		@NotNull(message = "Target account number is required") @Size(min = 10, max = 10, message = "Target account number must be exactly 10 characters") @Schema(description = "The 10-digit target account number", example = "0987654321") String targetAccountNumber,

		@NotNull(message = "Transfer amount is required") @Positive(message = "Transfer amount must be greater than zero") @Schema(description = "The amount to transfer", example = "150.00") BigDecimal amount) {
}
