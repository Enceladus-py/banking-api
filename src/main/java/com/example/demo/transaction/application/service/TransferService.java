package com.example.demo.transaction.application.service;

import java.time.Instant;
import java.util.UUID;

import com.example.demo.account.application.port.in.AccountOperationsPort;
import com.example.demo.account.domain.model.Account;
import com.example.demo.common.application.annotation.TransactionalUseCase;
import com.example.demo.common.domain.exception.EntityNotFoundException;
import com.example.demo.transaction.application.port.in.TransferMoneyUseCase;
import com.example.demo.transaction.application.port.out.EventPublisher;
import com.example.demo.transaction.application.port.out.TransactionRecordRepository;
import com.example.demo.transaction.domain.event.TransactionPendingEvent;
import com.example.demo.transaction.domain.model.TransactionRecord;
import com.example.demo.transaction.domain.model.TransactionRecord.TransactionType;

/**
 * Initiates a transfer by creating a PENDING ledger entry and enqueuing a
 * {@link TransactionPendingEvent} to the outbox.
 *
 * <p>
 * The actual balance mutation (debit source, credit target) is performed
 * asynchronously by {@link ProcessTransactionService} when the outbox scheduler
 * dispatches the event — exactly the same flow as Deposit and Withdrawal. This
 * keeps the consistency model uniform across all money movements.
 *
 * <p>
 * Ownership and account existence are validated here (in the initiating
 * transaction) so that the caller receives immediate feedback on obvious
 * errors. The pessimistic write lock is held by
 * {@link ProcessTransactionService} when it performs the actual mutation.
 */
@TransactionalUseCase
public class TransferService implements TransferMoneyUseCase {

	private final AccountOperationsPort accountOperationsPort;
	private final TransactionRecordRepository transactionRecordRepository;
	private final EventPublisher eventPublisher;

	/**
	 * Constructs a new TransferService with the required ports.
	 *
	 * @param accountOperationsPort
	 *            the port for account queries and operations
	 * @param transactionRecordRepository
	 *            the repository for storing transaction ledger records
	 * @param eventPublisher
	 *            the publisher for transaction events
	 */
	public TransferService(AccountOperationsPort accountOperationsPort,
			TransactionRecordRepository transactionRecordRepository, EventPublisher eventPublisher) {
		this.accountOperationsPort = accountOperationsPort;
		this.transactionRecordRepository = transactionRecordRepository;
		this.eventPublisher = eventPublisher;
	}

	@Override
	public TransactionRecord transfer(TransferCommand command) {
		// Validate source account existence and ownership (no write lock needed yet)
		Account sourceAccount = accountOperationsPort.findByAccountNumber(command.sourceAccountNumber())
				.orElseThrow(() -> new EntityNotFoundException("Source account not found"));

		if (!sourceAccount.isOwnedBy(command.requesterId())) {
			throw new SecurityException("You are not authorized to transfer money from this account");
		}

		// Validate target account existence
		accountOperationsPort.findByAccountNumber(command.targetAccountNumber())
				.orElseThrow(() -> new EntityNotFoundException("Target account not found"));

		// Create PENDING ledger entry — the outbox scheduler will settle it
		TransactionRecord pendingTx = TransactionRecord.createNew(command.sourceAccountNumber(),
				command.targetAccountNumber(), command.amount(), TransactionType.TRANSFER);
		transactionRecordRepository.save(pendingTx);

		// Enqueue event to outbox (written atomically in the same transaction)
		eventPublisher.publish(new TransactionPendingEvent(UUID.randomUUID(), pendingTx.getId(), Instant.now(),
				command.sourceAccountNumber(), command.targetAccountNumber(), command.amount(),
				TransactionType.TRANSFER, command.requesterId()));

		return pendingTx;
	}
}
