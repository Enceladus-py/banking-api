package com.example.demo.infrastructure.adapter.in.web;

import com.example.demo.application.port.in.CreateAccountUseCase;
import com.example.demo.application.port.in.DepositMoneyUseCase;
import com.example.demo.application.port.in.WithdrawMoneyUseCase;
import com.example.demo.domain.model.Account;
import com.example.demo.infrastructure.adapter.in.web.dto.AccountResponse;
import com.example.demo.infrastructure.adapter.in.web.dto.TransactionRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    // Inject the Inbound Port (Spring will provide the BankAccountService bean we
    // created earlier)
    private final CreateAccountUseCase createAccountUseCase;
    private final DepositMoneyUseCase depositMoneyUseCase;
    private final WithdrawMoneyUseCase withdrawMoneyUseCase;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@RequestHeader("X-User-Id") String requesterId) {
        var command = new CreateAccountUseCase.CreateAccountCommand(requesterId);
        Account account = createAccountUseCase.createAccount(command);
        return ResponseEntity.ok(toResponse(account));
    }

    @PutMapping("/deposit")
    public ResponseEntity<AccountResponse> depositMoney(
            @Valid @RequestBody TransactionRequest request,
            @RequestHeader("X-User-Id") String requesterId) {

        var command = new DepositMoneyUseCase.DepositCommand(request.accountNumber(), request.amount(),
                requesterId);
        Account account = depositMoneyUseCase.deposit(command);
        return ResponseEntity.ok(toResponse(account));
    }

    @PutMapping("/withdraw")
    public ResponseEntity<AccountResponse> withdrawMoney(
            @Valid @RequestBody TransactionRequest request,
            @RequestHeader("X-User-Id") String requesterId) {

        var command = new WithdrawMoneyUseCase.WithdrawCommand(request.accountNumber(), request.amount(),
                requesterId);
        Account account = withdrawMoneyUseCase.withdraw(command);
        return ResponseEntity.ok(toResponse(account));
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(account.getId(), account.getOwnerId(), account.getAccountNumber(),
                account.getBalance());
    }
}