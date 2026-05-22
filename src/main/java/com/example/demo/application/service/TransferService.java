package com.example.demo.application.service;

import com.example.demo.application.port.in.TransferMoneyUseCase;
import com.example.demo.application.port.out.AccountRepository;
import com.example.demo.application.port.out.TransactionRecordRepository;
import com.example.demo.domain.model.Account;
import com.example.demo.domain.model.TransactionRecord;
import com.example.demo.domain.model.TransactionRecord.TransactionType;

public class TransferService implements TransferMoneyUseCase {

    private final AccountRepository accountRepository;
    private final TransactionRecordRepository transactionRecordRepository;

    public TransferService(AccountRepository accountRepository,
            TransactionRecordRepository transactionRecordRepository) {
        this.accountRepository = accountRepository;
        this.transactionRecordRepository = transactionRecordRepository;
    }

    @Override
    public void transfer(TransferCommand command) {
        // Load both accounts
        Account sourceAccount = accountRepository.findByAccountNumber(command.sourceAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Source account not found"));

        if (!sourceAccount.isOwnedBy(command.requesterId())) {
            throw new SecurityException("You are not authorized to transfer money from this account");
        }
        
        Account targetAccount = accountRepository.findByAccountNumber(command.targetAccountNumber())
                .orElseThrow(() -> new IllegalArgumentException("Target account not found"));

        // Execute Domain logic
        sourceAccount.withdraw(command.amount());
        targetAccount.deposit(command.amount());

        // Save updated states
        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);

        // Create and Save Immutable Ledger Record
        TransactionRecord ledgerEntry = new TransactionRecord(
                sourceAccount.getAccountNumber(),
                targetAccount.getAccountNumber(),
                command.amount(),
                TransactionType.TRANSFER);
        transactionRecordRepository.save(ledgerEntry);
    }
}