package com.example.demo.application.service;

import com.example.demo.application.port.in.CreateAccountUseCase;
import com.example.demo.application.port.out.AccountRepository;
import com.example.demo.domain.model.Account;

public class BankAccountService implements CreateAccountUseCase {

    private final AccountRepository accountRepository;

    // Dependency Injection via constructor (Spring will wire this later in the
    // Infrastructure layer)
    public BankAccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account createAccount(CreateAccountCommand command) {
        // 1. Delegate business rules to the Domain entity
        Account newAccount = new Account(command.name(), command.surname());

        // 2. Delegate persistence to the Outbound Port
        return accountRepository.save(newAccount);
    }
}