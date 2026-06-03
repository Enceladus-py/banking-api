package com.example.demo.account.infrastructure.adapter.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.account.application.port.in.CreateAccountUseCase;
import com.example.demo.account.application.port.in.GetAccountUseCase;
import com.example.demo.account.domain.model.Account;
import com.example.demo.account.infrastructure.adapter.in.web.dto.AccountResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

	// Inject the Inbound Port (Spring will provide the BankAccountService bean we
	// created earlier)
	private final CreateAccountUseCase createAccountUseCase;
	private final GetAccountUseCase getAccountUseCase;

	@PostMapping
	public ResponseEntity<AccountResponse> createAccount(@RequestHeader("X-User-Id") String requesterId) {
		var command = new CreateAccountUseCase.CreateAccountCommand(requesterId);
		Account account = createAccountUseCase.createAccount(command);
		return ResponseEntity.ok(toResponse(account));
	}

	@GetMapping("/{accountNumber}")
	public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountNumber,
			@RequestHeader("X-User-Id") String requesterId) {

		Account account = getAccountUseCase.getAccount(accountNumber, requesterId);
		return ResponseEntity.ok(toResponse(account));
	}

	private AccountResponse toResponse(Account account) {
		return new AccountResponse(account.getId(), account.getOwnerId(), account.getAccountNumber(),
				account.getBalance());
	}
}
