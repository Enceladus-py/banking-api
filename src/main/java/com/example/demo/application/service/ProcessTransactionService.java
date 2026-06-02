package com.example.demo.application.service;

import com.example.demo.application.port.in.ProcessTransactionUseCase;
import com.example.demo.application.port.out.AccountRepository;
import com.example.demo.application.port.out.TransactionRecordRepository;
import com.example.demo.domain.event.TransactionEvent;
import com.example.demo.domain.event.TransactionPendingEvent;
import com.example.demo.domain.model.Account;
import com.example.demo.domain.model.TransactionRecord;
import com.example.demo.domain.model.TransactionRecord.TransactionStatus;
import com.example.demo.domain.exception.EntityNotFoundException;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Transactional
@Slf4j
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
            if (event.type() == TransactionEvent.TransactionType.DEPOSIT) {
                // Fetch with write lock for safety during state mutation
                Account target = accountRepository.findByAccountNumberForWrite(event.targetAccountNumber())
                        .orElseThrow(() -> new EntityNotFoundException("Target account not found"));
                
                target.deposit(event.amount());
                accountRepository.save(target);

            } else if (event.type() == TransactionEvent.TransactionType.WITHDRAWAL) {
                // Fetch with write lock for safety during state mutation
                Account source = accountRepository.findByAccountNumberForWrite(event.sourceAccountNumber())
                        .orElseThrow(() -> new EntityNotFoundException("Source account not found"));
                
                source.withdraw(event.amount());
                accountRepository.save(source);
            }

            // Update Transaction state to COMPLETED
            TransactionRecord completedTx = new TransactionRecord(
                    tx.getId(),
                    tx.getSourceAccountNumber(),
                    tx.getTargetAccountNumber(),
                    tx.getAmount(),
                    tx.getType(),
                    tx.getTimestamp(),
                    TransactionStatus.COMPLETED,
                    null
            );
            transactionRecordRepository.save(completedTx);
            log.info("Transaction {} successfully completed", tx.getId());

        } catch (Exception e) {
            log.error("Transaction {} failed to process: {}", tx.getId(), e.getMessage());
            
            // Update Transaction state to FAILED
            TransactionRecord failedTx = new TransactionRecord(
                    tx.getId(),
                    tx.getSourceAccountNumber(),
                    tx.getTargetAccountNumber(),
                    tx.getAmount(),
                    tx.getType(),
                    tx.getTimestamp(),
                    TransactionStatus.FAILED,
                    e.getMessage()
            );
            transactionRecordRepository.save(failedTx);
        }
    }
}
