package com.example.demo.transaction.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Request payload for a transaction (deposit or withdrawal).
 *
 * @param accountNumber
 *            the account number
 * @param amount
 *            the transaction amount
 */
@Schema(description = "Request payload for a deposit or withdrawal transaction")
public record TransactionRequest(
		@NotBlank(message = "Account number is required") @Size(min = 10, max = 10, message = "Account number must be exactly 10 characters") @Schema(description = "The 10-digit account number", example = "1234567890") String accountNumber,

		@NotNull(message = "Amount is required") @Positive(message = "Amount must be greater than zero") @Schema(description = "The amount to deposit or withdraw", example = "100.00") BigDecimal amount) {
}
