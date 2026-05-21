package com.example.demo.application.service;

import com.example.demo.application.port.in.CreateAccountUseCase;
import com.example.demo.application.port.in.DepositMoneyUseCase;
import com.example.demo.application.port.out.AccountRepository;
import com.example.demo.domain.model.Account;

import java.util.UUID;

public class BankAccountService implements CreateAccountUseCase, DepositMoneyUseCase {

    private final AccountRepository accountRepository;

    // Dependency Injection via constructor (Spring will wire this later in the
    // Infrastructure layer)
    public BankAccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account createAccount(CreateAccountCommand command) {
        String uniqueAccountNumber = generateUniqueAccountNumber();
        Account account = new Account(command.name(), command.surname(), uniqueAccountNumber);
        return accountRepository.save(account);
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            accountNumber = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        } while (accountRepository.findByAccountNumber(accountNumber).isPresent());

        return accountNumber;
    }

    @Override
    public Account deposit(DepositCommand command) {
        // 1. Fetch the account
        Account account = accountRepository.findByAccountNumber(command.accountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        // 2. Execute core business logic (the Domain Model protects itself against bad
        // amounts)
        account.deposit(command.amount());

        // 3. Save the updated state
        return accountRepository.save(account);
    }
}