package com.example.demo.transaction.infrastructure.adapter.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.example.demo.common.application.port.in.dto.PageRequest;
import com.example.demo.common.application.port.in.dto.PageResult;
import com.example.demo.common.security.CustomUserDetails;
import com.example.demo.transaction.application.port.in.DepositMoneyUseCase;
import com.example.demo.transaction.application.port.in.GetAccountTransactionsUseCase;
import com.example.demo.transaction.application.port.in.GetTransactionUseCase;
import com.example.demo.transaction.application.port.in.WithdrawMoneyUseCase;
import com.example.demo.transaction.domain.model.TransactionRecord;
import com.example.demo.transaction.infrastructure.adapter.in.web.dto.TransactionRequest;
import com.example.demo.transaction.infrastructure.adapter.in.web.dto.TransactionResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controller for deposit, withdrawal, and transaction query endpoints.
 */
@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Endpoints for deposit, withdrawal operations, and transaction audit trails")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

	private final GetAccountTransactionsUseCase getAccountTransactionsUseCase;
	private final GetTransactionUseCase getTransactionUseCase;
	private final DepositMoneyUseCase depositMoneyUseCase;
	private final WithdrawMoneyUseCase withdrawMoneyUseCase;

	/**
	 * Constructs a new TransactionController with the required use cases.
	 *
	 * @param getAccountTransactionsUseCase
	 *            the use case to query transactions of an account
	 * @param getTransactionUseCase
	 *            the use case to query details of a single transaction
	 * @param depositMoneyUseCase
	 *            the use case to perform deposits
	 * @param withdrawMoneyUseCase
	 *            the use case to perform withdrawals
	 */
	public TransactionController(GetAccountTransactionsUseCase getAccountTransactionsUseCase,
			GetTransactionUseCase getTransactionUseCase, DepositMoneyUseCase depositMoneyUseCase,
			WithdrawMoneyUseCase withdrawMoneyUseCase) {
		this.getAccountTransactionsUseCase = getAccountTransactionsUseCase;
		this.getTransactionUseCase = getTransactionUseCase;
		this.depositMoneyUseCase = depositMoneyUseCase;
		this.withdrawMoneyUseCase = withdrawMoneyUseCase;
	}

	/**
	 * Deposits money into a bank account.
	 *
	 * @param request
	 *            the transaction request details
	 * @param userDetails
	 *            the authenticated user details
	 * @return the response containing transaction details
	 */
	@PostMapping("/deposit")
	@Operation(summary = "Deposit money", description = "Asynchronously deposits an amount to the specified account. Only the owner can deposit.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Deposit transaction accepted for processing"),
			@ApiResponse(responseCode = "400", description = "Invalid deposit amount or account details"),
			@ApiResponse(responseCode = "403", description = "Requester does not own the account")})
	public ResponseEntity<TransactionResponse> depositMoney(@Valid @RequestBody TransactionRequest request,
			@AuthenticationPrincipal @Parameter(hidden = true) CustomUserDetails userDetails) {

		var command = new DepositMoneyUseCase.DepositCommand(request.accountNumber(), request.amount(),
				userDetails.getId());
		var tx = depositMoneyUseCase.deposit(command);
		return ResponseEntity.accepted().body(TransactionResponse.from(tx));
	}

	/**
	 * Withdraws money from a bank account.
	 *
	 * @param request
	 *            the transaction request details
	 * @param userDetails
	 *            the authenticated user details
	 * @return the response containing transaction details
	 */
	@PostMapping("/withdraw")
	@Operation(summary = "Withdraw money", description = "Asynchronously withdraws an amount from the specified account. Requester must own the account and have sufficient funds.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "202", description = "Withdrawal transaction accepted for processing"),
			@ApiResponse(responseCode = "400", description = "Invalid withdrawal amount, account details, or insufficient funds"),
			@ApiResponse(responseCode = "403", description = "Requester does not own the account")})
	public ResponseEntity<TransactionResponse> withdrawMoney(@Valid @RequestBody TransactionRequest request,
			@AuthenticationPrincipal @Parameter(hidden = true) CustomUserDetails userDetails) {

		var command = new WithdrawMoneyUseCase.WithdrawCommand(request.accountNumber(), request.amount(),
				userDetails.getId());
		var tx = withdrawMoneyUseCase.withdraw(command);
		return ResponseEntity.accepted().body(TransactionResponse.from(tx));
	}

	/**
	 * Retrieves transactions for a bank account with pagination.
	 *
	 * @param accountNumber
	 *            the bank account number
	 * @param page
	 *            the page number (0-based)
	 * @param size
	 *            the size of the page
	 * @param userDetails
	 *            the authenticated user details
	 * @return the page result containing transaction records
	 */
	@GetMapping("/{accountNumber}")
	@Operation(summary = "Get account transactions", description = "Retrieves a paginated list of transaction records for the given account. Only accessible by owner.")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Transactions retrieved successfully"),
			@ApiResponse(responseCode = "403", description = "Requester does not own the account"),
			@ApiResponse(responseCode = "404", description = "Account not found")})
	public ResponseEntity<PageResult<TransactionRecord>> getTransactions(
			@PathVariable @Parameter(description = "The 10-digit account number to query", example = "1234567890") String accountNumber,
			@RequestParam(defaultValue = "0") @Parameter(description = "Zero-based page index", example = "0") int page,
			@RequestParam(defaultValue = "10") @Parameter(description = "Size of the page to retrieve", example = "10") int size,
			@AuthenticationPrincipal @Parameter(hidden = true) CustomUserDetails userDetails) {

		PageRequest pageRequest = new PageRequest(page, size);
		PageResult<TransactionRecord> result = getAccountTransactionsUseCase.getTransactions(accountNumber, pageRequest,
				userDetails.getId());

		return ResponseEntity.ok(result);
	}

	/**
	 * Retrieves a single transaction record by ID.
	 *
	 * @param transactionId
	 *            the transaction ID
	 * @param userDetails
	 *            the authenticated user details
	 * @return the response containing transaction details
	 */
	@GetMapping("/id/{transactionId}")
	@Operation(summary = "Get transaction details", description = "Retrieves details of a specific transaction by its unique identifier. Only accessible by account owner.")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Transaction details found"),
			@ApiResponse(responseCode = "403", description = "Access denied for requester"),
			@ApiResponse(responseCode = "404", description = "Transaction not found")})
	public ResponseEntity<TransactionResponse> getTransactionById(
			@PathVariable @Parameter(description = "The unique identifier of the transaction", example = "123e4567-e89b-12d3-a456-426614174000") String transactionId,
			@AuthenticationPrincipal @Parameter(hidden = true) CustomUserDetails userDetails) {

		TransactionRecord record = getTransactionUseCase.getTransaction(transactionId, userDetails.getId());
		return ResponseEntity.ok(TransactionResponse.from(record));
	}
}
