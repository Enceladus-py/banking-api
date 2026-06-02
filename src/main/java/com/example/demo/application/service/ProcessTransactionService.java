package com.example.demo.application.service;

import com.example.demo.application.annotation.TransactionalUseCase;

import com.example.demo.application.port.in.ProcessTransactionUseCase;
import com.example.demo.application.port.out.AccountRepository;
import com.example.demo.application.port.out.TransactionRecordRepository;
import com.example.demo.domain.event.TransactionPendingEvent;
import com.example.demo.domain.model.Account;
import com.example.demo.domain.model.TransactionRecord;
import com.example.demo.domain.model.TransactionRecord.TransactionStatus;
import com.example.demo.domain.model.TransactionRecord.TransactionType;
import com.example.demo.domain.exception.EntityNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@TransactionalUseCase
public class ProcessTransactionService implements ProcessTransactionUseCase {

    private final AccountRepository accountRepository;
    private final TransactionRecordRepository transactionRecordRepository;

    public ProcessTransactionService(AccountRepository accountRepository,
                                     TransactionRecordRepository transactionRecordRepository) {
        this.accountRepository = accountRepository;
        this.transactionRecordRepository = transactionRecordRepository;
    }

    @Override
    public void process(TransactionPendingEvent event) {
        log.info("Processing pending event for transaction ID: {}", event.transactionId());

        TransactionRecord tx = transactionRecordRepository.findById(event.transactionId())
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found for ID: " + event.transactionId()));

        if (tx.getStatus() != TransactionStatus.PENDING) {
            log.info("Transaction {} already processed with status: {}", tx.getId(), tx.getStatus());
            return;
        }

        try {
            if (event.type() == TransactionType.DEPOSIT) {
                // Fetch with write lock for safety during state mutation
                Account target = accountRepository.lockAndLoad(event.targetAccountNumber())
                        .orElseThrow(() -> new EntityNotFoundException("Target account not found"));

                target.deposit(event.amount());
                accountRepository.save(target);

            } else if (event.type() == TransactionType.WITHDRAWAL) {
                // Fetch with write lock for safety during state mutation
                Account source = accountRepository.lockAndLoad(event.sourceAccountNumber())
                        .orElseThrow(() -> new EntityNotFoundException("Source account not found"));

                source.withdraw(event.amount());
                accountRepository.save(source);

            } else if (event.type() == TransactionType.TRANSFER) {
                // Lock both accounts in a consistent alphabetical order to prevent deadlocks
                boolean sourceFirst = event.sourceAccountNumber().compareTo(event.targetAccountNumber()) <= 0;
                final String firstKey  = sourceFirst ? event.sourceAccountNumber() : event.targetAccountNumber();
                final String secondKey = sourceFirst ? event.targetAccountNumber() : event.sourceAccountNumber();

                Account a = accountRepository.lockAndLoad(firstKey)
                        .orElseThrow(() -> new EntityNotFoundException("Account not found: " + firstKey));
                Account b = accountRepository.lockAndLoad(secondKey)
                        .orElseThrow(() -> new EntityNotFoundException("Account not found: " + secondKey));

                // Re-assign source/target after ordering
                Account source = a.getAccountNumber().equals(event.sourceAccountNumber()) ? a : b;
                Account target = a.getAccountNumber().equals(event.targetAccountNumber()) ? a : b;

                source.withdraw(event.amount());
                target.deposit(event.amount());
                accountRepository.save(source);
                accountRepository.save(target);
            }

            // Transition domain object to COMPLETED — no manual reconstruction needed
            transactionRecordRepository.save(tx.complete());
            log.info("Transaction {} successfully completed", tx.getId());

        } catch (Exception e) {
            log.error("Transaction {} failed to process: {}", tx.getId(), e.getMessage());

            // Transition domain object to FAILED with reason
            transactionRecordRepository.save(tx.fail(e.getMessage()));
        }
    }
}
