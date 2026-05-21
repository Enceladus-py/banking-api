package com.example.demo.application.service;

import com.example.demo.application.port.in.CreateAccountUseCase;
import com.example.demo.application.port.in.DepositMoneyUseCase;
import com.example.demo.application.port.in.WithdrawMoneyUseCase;
import com.example.demo.application.port.out.AccountRepository;
import com.example.demo.application.port.out.TransactionRecordRepository;
import com.example.demo.domain.model.Account;
import com.example.demo.domain.model.TransactionRecord;
import com.example.demo.domain.model.TransactionRecord.TransactionType;

import java.util.UUID;

public class BankAccountService implements CreateAccountUseCase, DepositMoneyUseCase, WithdrawMoneyUseCase {

    private final AccountRepository accountRepository;
    private final TransactionRecordRepository transactionRecordRepository;

    // Dependency Injection via constructor (Spring will wire this later in the
    // Infrastructure layer)
    public BankAccountService(AccountRepository accountRepository,
            TransactionRecordRepository transactionRecordRepository) {
        this.accountRepository = accountRepository;
        this.transactionRecordRepository = transactionRecordRepository;
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
        Account savedAccount = accountRepository.save(account);

        // 4. Create and Save Immutable Ledger Record
        TransactionRecord ledgerEntry = new TransactionRecord(
                null, // Deposits have no source
                account.getAccountNumber(), // Target is this account
                command.amount(),
                TransactionType.DEPOSIT);
        transactionRecordRepository.save(ledgerEntry);

        return savedAccount;
    }

    @Override
    public Account withdraw(WithdrawCommand command) {
        // 1. Load from DB
        Account account = accountRepository.findByAccountNumber(command.accountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        // 2. Execute business logic (Domain protects itself against over-drafting)
        account.withdraw(command.amount());

        // 3. Save Account State
        Account savedAccount = accountRepository.save(account);

        // 4. Create and Save Immutable Ledger Record
        TransactionRecord ledgerEntry = new TransactionRecord(
                account.getAccountNumber(), // Source is this account
                null, // Withdrawals have no target
                command.amount(),
                TransactionType.WITHDRAWAL);
        transactionRecordRepository.save(ledgerEntry);

        return savedAccount;
    }
}