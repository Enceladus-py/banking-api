package com.example.demo.infrastructure.adapter.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.application.port.in.CreateAccountUseCase;
import com.example.demo.application.port.in.DepositMoneyUseCase;
import com.example.demo.application.port.in.GetAccountUseCase;
import com.example.demo.application.port.in.WithdrawMoneyUseCase;
import com.example.demo.domain.model.Account;
import com.example.demo.infrastructure.adapter.in.web.dto.AccountResponse;
import com.example.demo.infrastructure.adapter.in.web.dto.TransactionRequest;
import com.example.demo.infrastructure.adapter.in.web.dto.TransactionResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

	// Inject the Inbound Port (Spring will provide the BankAccountService bean we
	// created earlier)
	private final CreateAccountUseCase createAccountUseCase;
	private final DepositMoneyUseCase depositMoneyUseCase;
	private final WithdrawMoneyUseCase withdrawMoneyUseCase;
	private final GetAccountUseCase getAccountUseCase;

	@PostMapping
	public ResponseEntity<AccountResponse> createAccount(@RequestHeader("X-User-Id") String requesterId) {
		var command = new CreateAccountUseCase.CreateAccountCommand(requesterId);
		Account account = createAccountUseCase.createAccount(command);
		return ResponseEntity.ok(toResponse(account));
	}

	@PostMapping("/deposit")
	public ResponseEntity<TransactionResponse> depositMoney(@Valid @RequestBody TransactionRequest request,
			@RequestHeader("X-User-Id") String requesterId) {

		var command = new DepositMoneyUseCase.DepositCommand(request.accountNumber(), request.amount(), requesterId);
		var tx = depositMoneyUseCase.deposit(command);
		return ResponseEntity.accepted().body(TransactionResponse.from(tx));
	}

	@PostMapping("/withdraw")
	public ResponseEntity<TransactionResponse> withdrawMoney(@Valid @RequestBody TransactionRequest request,
			@RequestHeader("X-User-Id") String requesterId) {

		var command = new WithdrawMoneyUseCase.WithdrawCommand(request.accountNumber(), request.amount(), requesterId);
		var tx = withdrawMoneyUseCase.withdraw(command);
		return ResponseEntity.accepted().body(TransactionResponse.from(tx));
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
