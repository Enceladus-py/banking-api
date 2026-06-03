package com.example.demo.account.infrastructure.adapter.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.account.application.port.in.CreateAccountUseCase;
import com.example.demo.account.application.port.in.GetAccountUseCase;
import com.example.demo.account.domain.model.Account;
import com.example.demo.account.infrastructure.adapter.in.web.dto.AccountResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller for banking account endpoints.
 */
@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Accounts", description = "Endpoints for bank account creation and retrieval")
public class AccountController {

	// Inject the Inbound Port (Spring will provide the BankAccountService bean we
	// created earlier)
	private final CreateAccountUseCase createAccountUseCase;
	private final GetAccountUseCase getAccountUseCase;

	/**
	 * Constructs a new AccountController with the required use cases.
	 *
	 * @param createAccountUseCase
	 *            the use case to create bank accounts
	 * @param getAccountUseCase
	 *            the use case to retrieve account details
	 */
	public AccountController(CreateAccountUseCase createAccountUseCase, GetAccountUseCase getAccountUseCase) {
		this.createAccountUseCase = createAccountUseCase;
		this.getAccountUseCase = getAccountUseCase;
	}

	/**
	 * Creates a new account for the requester.
	 *
	 * @param requesterId
	 *            the user ID of the account owner
	 * @return the response containing created account details
	 */
	@PostMapping
	@Operation(summary = "Create account", description = "Creates a new bank account with an initial balance of zero for the requester")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Account successfully created"),
			@ApiResponse(responseCode = "400", description = "Invalid request or user details"),
			@ApiResponse(responseCode = "404", description = "Owner user profile not found")})
	public ResponseEntity<AccountResponse> createAccount(
			@RequestHeader("X-User-Id") @Parameter(description = "The ID of the user requesting account creation", example = "123e4567-e89b-12d3-a456-426614174000") String requesterId) {
		var command = new CreateAccountUseCase.CreateAccountCommand(requesterId);
		Account account = createAccountUseCase.createAccount(command);
		return ResponseEntity.ok(toResponse(account));
	}

	/**
	 * Retrieves bank account details by account number.
	 *
	 * @param accountNumber
	 *            the bank account number
	 * @param requesterId
	 *            the user ID of the requester
	 * @return the response containing account details
	 */
	@GetMapping("/{accountNumber}")
	@Operation(summary = "Get account details", description = "Retrieves the account information for the given account number. Accessible only by owner or operator.")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Account found"),
			@ApiResponse(responseCode = "403", description = "Access denied for requester"),
			@ApiResponse(responseCode = "404", description = "Account not found")})
	public ResponseEntity<AccountResponse> getAccount(
			@PathVariable @Parameter(description = "The 10-digit account number to retrieve", example = "1234567890") String accountNumber,
			@RequestHeader("X-User-Id") @Parameter(description = "The ID of the user requesting account information", example = "123e4567-e89b-12d3-a456-426614174000") String requesterId) {

		Account account = getAccountUseCase.getAccount(accountNumber, requesterId);
		return ResponseEntity.ok(toResponse(account));
	}

	private AccountResponse toResponse(Account account) {
		return new AccountResponse(account.getId(), account.getOwnerId(), account.getAccountNumber(),
				account.getBalance());
	}
}
