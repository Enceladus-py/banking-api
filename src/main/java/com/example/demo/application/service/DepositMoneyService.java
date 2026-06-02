package com.example.demo.application.service;

import com.example.demo.application.port.in.DepositMoneyUseCase;
import com.example.demo.application.port.out.AccountRepository;
import com.example.demo.application.port.out.TransactionRecordRepository;
import com.example.demo.application.port.out.EventPublisher;
import com.example.demo.domain.event.TransactionPendingEvent;
import com.example.demo.domain.model.Account;
import com.example.demo.domain.model.TransactionRecord;
import com.example.demo.domain.model.TransactionRecord.TransactionType;
import com.example.demo.domain.exception.EntityNotFoundException;

import java.time.Instant;
import java.util.UUID;

public class DepositMoneyService implements DepositMoneyUseCase {

    private final AccountRepository accountRepository;
    private final TransactionRecordRepository transactionRecordRepository;
    private final EventPublisher eventPublisher;

    public DepositMoneyService(AccountRepository accountRepository,
                               TransactionRecordRepository transactionRecordRepository,
                               EventPublisher eventPublisher) {
        this.accountRepository = accountRepository;
        this.transactionRecordRepository = transactionRecordRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Account deposit(DepositCommand command) {
        // Fetch the account (No lock needed for initiating the pending state)
        Account account = accountRepository.findByAccountNumber(command.accountId())
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        // Enforce ownership
        if (!account.isOwnedBy(command.requesterId())) {
            throw new SecurityException("You are not authorized to deposit into this account");
        }

        // Create and Save PENDING Ledger Record
        TransactionRecord pendingTx = new TransactionRecord(
                null, // Deposits have no source
                account.getAccountNumber(), // Target is this account
                command.amount(),
                TransactionType.DEPOSIT);
        transactionRecordRepository.save(pendingTx);

        // Publish TransactionPendingEvent
        eventPublisher.publish(new TransactionPendingEvent(
                UUID.randomUUID(),
                pendingTx.getId(),
                Instant.now(),
                null,
                account.getAccountNumber(),
                command.amount(),
                TransactionType.DEPOSIT,
                command.requesterId()
        ));

        return account;
    }
}
