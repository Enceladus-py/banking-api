package com.example.demo.infrastructure.adapter.in.web;

import com.example.demo.application.port.in.CreateAccountUseCase;
import com.example.demo.application.port.in.DepositMoneyUseCase;
import com.example.demo.domain.model.Account;
import com.example.demo.infrastructure.adapter.in.web.dto.AccountResponse;
import com.example.demo.infrastructure.adapter.in.web.dto.CreateAccountRequest;
import com.example.demo.infrastructure.adapter.in.web.dto.DepositRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {

        // 1. Map incoming HTTP Request to the core Use Case Command
        CreateAccountUseCase.CreateAccountCommand command = new CreateAccountUseCase.CreateAccountCommand(
                request.name(), request.surname());

        // 2. Execute the core use case
        Account newAccount = createAccountUseCase.createAccount(command);

        // 3. Map the resulting Domain Model back to an HTTP Response DTO
        AccountResponse response = new AccountResponse(
                newAccount.getId(),
                newAccount.getName(),
                newAccount.getSurname(),
                newAccount.getAccountNumber(),
                newAccount.getBalance());

        // 4. Return HTTP 201 Created
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/deposit")
    public ResponseEntity<AccountResponse> depositMoney(@Valid @RequestBody DepositRequest request) {

        // Maps clean data from validated DTO into our internal command
        DepositMoneyUseCase.DepositCommand command = new DepositMoneyUseCase.DepositCommand(request.accountNumber(),
                request.amount());

        Account updatedAccount = depositMoneyUseCase.deposit(command);

        AccountResponse response = new AccountResponse(
                updatedAccount.getId(),
                updatedAccount.getName(),
                updatedAccount.getSurname(),
                updatedAccount.getAccountNumber(),
                updatedAccount.getBalance());

        return ResponseEntity.ok(response);
    }
}