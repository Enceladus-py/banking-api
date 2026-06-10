package com.fintech.banking.coreapi.transaction.infrastructure.adapter.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.fintech.banking.coreapi.common.infrastructure.security.CustomUserDetails;
import com.fintech.banking.coreapi.transaction.application.port.in.TransferMoneyUseCase;
import com.fintech.banking.coreapi.transaction.infrastructure.adapter.in.web.dto.TransactionResponse;
import com.fintech.banking.coreapi.transaction.infrastructure.adapter.in.web.dto.TransferRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controller for handling bank transfers between accounts.
 */
@RestController
@RequestMapping("/api/transfers")
@Tag(name = "Transfers", description = "Endpoints for initiating funds transfers between bank accounts")
public class TransferController {

	private final TransferMoneyUseCase transferMoneyUseCase;

	/**
	 * Constructs a new TransferController with the required use case.
	 *
	 * @param transferMoneyUseCase
	 *            the use case to perform transfers
	 */
	public TransferController(TransferMoneyUseCase transferMoneyUseCase) {
		this.transferMoneyUseCase = transferMoneyUseCase;
	}

	/**
	 * Initiates a funds transfer transaction.
	 *
	 * @param request
	 *            the transfer request containing source, destination, and amount
	 * @param userDetails
	 *            the authenticated user details
	 * @return the response containing transaction details
	 */
	@PostMapping
	@Operation(summary = "Transfer money", description = "Asynchronously transfers funds from a source account to a destination account. Source account must be owned by the requester.")
	@ApiResponses(value = {@ApiResponse(responseCode = "202", description = "Transfer request accepted for processing"),
			@ApiResponse(responseCode = "400", description = "Invalid account numbers, transfer amount, or insufficient funds"),
			@ApiResponse(responseCode = "403", description = "Requester does not own the source account")})
	public ResponseEntity<TransactionResponse> transferMoney(@Valid @RequestBody TransferRequest request,
			@AuthenticationPrincipal @Parameter(hidden = true) CustomUserDetails userDetails) {

		TransferMoneyUseCase.TransferCommand command = new TransferMoneyUseCase.TransferCommand(
				request.sourceAccountNumber(), request.targetAccountNumber(), request.amount(), userDetails.getId());

		var tx = transferMoneyUseCase.transfer(command);

		return ResponseEntity.accepted().body(TransactionResponse.from(tx));
	}
}
