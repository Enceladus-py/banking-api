package com.fintech.banking.coreapi.account.infrastructure.adapter.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.fintech.banking.coreapi.account.application.port.in.CreateAccountUseCase;
import com.fintech.banking.coreapi.account.application.port.in.GetAccountUseCase;
import com.fintech.banking.coreapi.account.application.port.in.GetAccountsUseCase;
import com.fintech.banking.coreapi.account.domain.model.Account;
import com.fintech.banking.coreapi.account.infrastructure.adapter.in.web.dto.AccountResponse;
import com.fintech.banking.coreapi.common.application.port.in.dto.PageRequest;
import com.fintech.banking.coreapi.common.application.port.in.dto.PageResult;
import com.fintech.banking.coreapi.common.infrastructure.security.CustomUserDetails;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller for banking account endpoints.
 */
@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Accounts", description = "Endpoints for bank account creation and retrieval")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

	private final CreateAccountUseCase createAccountUseCase;
	private final GetAccountUseCase getAccountUseCase;
	private final GetAccountsUseCase getAccountsUseCase;

	/**
	 * Constructs a new AccountController with the required use cases.
	 *
	 * @param createAccountUseCase
	 *            the use case to create bank accounts
	 * @param getAccountUseCase
	 *            the use case to retrieve account details
	 * @param getAccountsUseCase
	 *            the use case to retrieve multiple accounts
	 */
	public AccountController(CreateAccountUseCase createAccountUseCase, GetAccountUseCase getAccountUseCase,
			GetAccountsUseCase getAccountsUseCase) {
		this.createAccountUseCase = createAccountUseCase;
		this.getAccountUseCase = getAccountUseCase;
		this.getAccountsUseCase = getAccountsUseCase;
	}

	/**
	 * Creates a new account for the requester.
	 *
	 * @param userDetails
	 *            the authenticated user details
	 * @return the response containing created account details
	 */
	@PostMapping
	@Operation(summary = "Create account", description = "Creates a new bank account with an initial balance of zero for the requester")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Account successfully created"),
			@ApiResponse(responseCode = "400", description = "Invalid request or user details"),
			@ApiResponse(responseCode = "404", description = "Owner user profile not found")})
	public ResponseEntity<AccountResponse> createAccount(
			@AuthenticationPrincipal @Parameter(hidden = true) CustomUserDetails userDetails) {
		var command = new CreateAccountUseCase.CreateAccountCommand(userDetails.getId());
		Account account = createAccountUseCase.createAccount(command);
		return ResponseEntity.ok(toResponse(account));
	}

	/**
	 * Retrieves bank account details by account number.
	 *
	 * @param accountNumber
	 *            the bank account number
	 * @param userDetails
	 *            the authenticated user details
	 * @return the response containing account details
	 */
	@GetMapping("/{accountNumber}")
	@Operation(summary = "Get account details", description = "Retrieves the account information for the given account number. Accessible only by owner or operator.")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Account found"),
			@ApiResponse(responseCode = "403", description = "Access denied for requester"),
			@ApiResponse(responseCode = "404", description = "Account not found")})
	public ResponseEntity<AccountResponse> getAccount(
			@PathVariable @Parameter(description = "The 10-digit account number to retrieve", example = "1234567890") String accountNumber,
			@AuthenticationPrincipal @Parameter(hidden = true) CustomUserDetails userDetails) {

		Account account = getAccountUseCase.getAccount(accountNumber, userDetails.getId());
		return ResponseEntity.ok(toResponse(account));
	}

	/**
	 * Retrieves all bank accounts for a specific user.
	 *
	 * @param page
	 *            the page number
	 * @param size
	 *            the page size
	 * @param userDetails
	 *            the authenticated user details
	 * @return the response containing a list of accounts
	 */
	@GetMapping
	@Operation(summary = "Get user accounts", description = "Retrieves all accounts owned by the requesting user.")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Accounts found"),
			@ApiResponse(responseCode = "400", description = "Invalid request")})
	public ResponseEntity<PageResult<AccountResponse>> getAccounts(
			@RequestParam(defaultValue = "0") @Parameter(description = "Page number (0-based)") int page,
			@RequestParam(defaultValue = "10") @Parameter(description = "Number of records per page") int size,
			@AuthenticationPrincipal @Parameter(hidden = true) CustomUserDetails userDetails) {

		var pageRequest = new PageRequest(page, size);
		PageResult<Account> accountPage = getAccountsUseCase.getAccountsByUserId(userDetails.getId(), pageRequest);

		var responsePage = new PageResult<>(accountPage.content().stream().map(this::toResponse).toList(),
				accountPage.pageNumber(), accountPage.pageSize(), accountPage.totalElements(),
				accountPage.totalPages());

		return ResponseEntity.ok(responsePage);
	}

	private AccountResponse toResponse(Account account) {
		return new AccountResponse(account.getId(), account.getOwnerId(), account.getAccountNumber(),
				account.getBalance());
	}
}
