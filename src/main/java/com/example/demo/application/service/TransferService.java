package com.example.demo.application.service;

import com.example.demo.application.port.in.TransferMoneyUseCase;
import com.example.demo.application.port.out.AccountRepository;
import com.example.demo.application.port.out.EventPublisher;
import com.example.demo.application.port.out.TransactionRecordRepository;
import com.example.demo.domain.event.TransactionPendingEvent;
import com.example.demo.domain.model.Account;
import com.example.demo.domain.model.TransactionRecord;
import com.example.demo.domain.model.TransactionRecord.TransactionType;
import com.example.demo.domain.exception.EntityNotFoundException;
import java.time.Instant;
import java.util.UUID;

/**
 * Initiates a transfer by creating a PENDING ledger entry and enqueuing a
 * {@link TransactionPendingEvent} to the outbox.
 *
 * <p>The actual balance mutation (debit source, credit target) is performed
 * asynchronously by {@link ProcessTransactionService} when the outbox scheduler
 * dispatches the event — exactly the same flow as Deposit and Withdrawal.
 * This keeps the consistency model uniform across all money movements.
 *
 * <p>Ownership and account existence are validated here (in the initiating
 * transaction) so that the caller receives immediate feedback on obvious errors.
 * The pessimistic write lock is held by {@link ProcessTransactionService} when
 * it performs the actual mutation.
 */
public class TransferService implements TransferMoneyUseCase {

    private final AccountRepository accountRepository;
    private final TransactionRecordRepository transactionRecordRepository;
    private final EventPublisher eventPublisher;

    public TransferService(AccountRepository accountRepository,
            TransactionRecordRepository transactionRecordRepository,
            EventPublisher eventPublisher) {
        this.accountRepository = accountRepository;
        this.transactionRecordRepository = transactionRecordRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void transfer(TransferCommand command) {
        // Validate source account existence and ownership (no write lock needed yet)
        Account sourceAccount = accountRepository.findByAccountNumber(command.sourceAccountNumber())
                .orElseThrow(() -> new EntityNotFoundException("Source account not found"));

        if (!sourceAccount.isOwnedBy(command.requesterId())) {
            throw new SecurityException("You are not authorized to transfer money from this account");
        }

        // Validate target account existence
        accountRepository.findByAccountNumber(command.targetAccountNumber())
                .orElseThrow(() -> new EntityNotFoundException("Target account not found"));

        // Create PENDING ledger entry — the outbox scheduler will settle it
        TransactionRecord pendingTx = new TransactionRecord(
                command.sourceAccountNumber(),
                command.targetAccountNumber(),
                command.amount(),
                TransactionType.TRANSFER);
        transactionRecordRepository.save(pendingTx);

        // Enqueue event to outbox (written atomically in the same transaction)
        eventPublisher.publish(new TransactionPendingEvent(
                UUID.randomUUID(),
                pendingTx.getId(),
                Instant.now(),
                command.sourceAccountNumber(),
                command.targetAccountNumber(),
                command.amount(),
                TransactionType.TRANSFER,
                command.requesterId()
        ));
    }
}