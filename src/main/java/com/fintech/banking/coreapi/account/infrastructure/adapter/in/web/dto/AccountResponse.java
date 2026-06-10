package com.fintech.banking.coreapi.account.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response payload representing a bank account.
 *
 * @param id
 *            the account ID
 * @param ownerId
 *            the owner's user ID
 * @param accountNumber
 *            the unique account number
 * @param balance
 *            the current balance of the account
 */
@Schema(description = "Response payload containing account details")
public record AccountResponse(
		@Schema(description = "The unique identifier of the account", example = "123e4567-e89b-12d3-a456-426614174000") String id,
		@Schema(description = "The unique identifier of the account owner", example = "123e4567-e89b-12d3-a456-426614174000") String ownerId,
		@Schema(description = "The 10-digit account number", example = "1234567890") String accountNumber,
		@Schema(description = "The current balance of the account", example = "1000.00") BigDecimal balance) {
}
