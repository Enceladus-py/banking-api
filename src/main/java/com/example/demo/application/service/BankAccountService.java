package com.example.demo.application.service;

import com.example.demo.application.port.in.CreateAccountUseCase;
import com.example.demo.application.port.in.DepositMoneyUseCase;
import com.example.demo.application.port.in.WithdrawMoneyUseCase;
import com.example.demo.application.port.out.AccountRepository;
import com.example.demo.application.port.out.TransactionRecordRepository;
import com.example.demo.application.port.out.UserRepository;
import com.example.demo.domain.model.Account;
import com.example.demo.domain.model.TransactionRecord;
import com.example.demo.domain.model.TransactionRecord.TransactionType;

import java.util.UUID;
import jakarta.transaction.Transactional;
import com.example.demo.domain.exception.EntityNotFoundException;

@Transactional
public class BankAccountService implements CreateAccountUseCase, DepositMoneyUseCase, WithdrawMoneyUseCase {

    private final AccountRepository accountRepository;
    private final TransactionRecordRepository transactionRecordRepository;
    private final UserRepository userRepository;

    // Dependency Injection via constructor (Spring will wire this later in the
    // Infrastructure layer)
    public BankAccountService(AccountRepository accountRepository,
            TransactionRecordRepository transactionRecordRepository,
            UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.transactionRecordRepository = transactionRecordRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Account createAccount(CreateAccountCommand command) {
        userRepository.findById(command.requesterId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        String uniqueAccountNumber = generateUniqueAccountNumber();
        Account account = Account.createNew(command.requesterId(), uniqueAccountNumber);
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
        // 1. Fetch the account (Pessimistic write lock)
        Account account = accountRepository.findByAccountNumberForWrite(command.accountId())
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        // Enforce ownership
        if (!account.isOwnedBy(command.requesterId())) {
            throw new SecurityException("You are not authorized to deposit into this account");
        }

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
        // Load from DB (Pessimistic write lock)
        Account account = accountRepository.findByAccountNumberForWrite(command.accountId())
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        // Enforce ownership
        if (!account.isOwnedBy(command.requesterId())) {
            throw new SecurityException("You are not authorized to withdraw from this account");
        }

        // Execute business logic (Domain protects itself against over-drafting)
        account.withdraw(command.amount());

        // Save Account State
        Account savedAccount = accountRepository.save(account);

        // Create and Save Immutable Ledger Record
        TransactionRecord ledgerEntry = new TransactionRecord(
                account.getAccountNumber(), // Source is this account
                null, // Withdrawals have no target
                command.amount(),
                TransactionType.WITHDRAWAL);
        transactionRecordRepository.save(ledgerEntry);

        return savedAccount;
    }
}