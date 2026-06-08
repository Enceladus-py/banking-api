package com.example.demo.transaction.application.service;

import java.time.Instant;
import java.util.UUID;

import com.example.demo.account.application.port.in.AccountOperationsPort;
import com.example.demo.account.domain.model.Account;
import com.example.demo.common.application.annotation.TransactionalUseCase;
import com.example.demo.common.domain.exception.EntityNotFoundException;
import com.example.demo.transaction.application.port.in.DepositMoneyUseCase;
import com.example.demo.transaction.application.port.out.EventPublisher;
import com.example.demo.transaction.application.port.out.TransactionRecordRepository;
import com.example.demo.transaction.domain.event.TransactionPendingEvent;
import com.example.demo.transaction.domain.model.TransactionRecord;
import com.example.demo.transaction.domain.model.TransactionRecord.TransactionType;

/**
 * Service class implementing the deposit money use case.
 */
@TransactionalUseCase
public class DepositMoneyService implements DepositMoneyUseCase {

	private final AccountOperationsPort accountOperationsPort;
	private final TransactionRecordRepository transactionRecordRepository;
	private final EventPublisher eventPublisher;

	/**
	 * Constructs a new DepositMoneyService with the required ports.
	 *
	 * @param accountOperationsPort
	 *            the port for account queries and operations
	 * @param transactionRecordRepository
	 *            the repository for storing transaction ledger records
	 * @param eventPublisher
	 *            the publisher for transaction events
	 */
	public DepositMoneyService(AccountOperationsPort accountOperationsPort,
			TransactionRecordRepository transactionRecordRepository, EventPublisher eventPublisher) {
		this.accountOperationsPort = accountOperationsPort;
		this.transactionRecordRepository = transactionRecordRepository;
		this.eventPublisher = eventPublisher;
	}

	@Override
	public TransactionRecord deposit(DepositCommand command) {
		// Fetch the account (No lock needed for initiating the pending state)
		Account account = accountOperationsPort.findByAccountNumber(command.accountId())
				.orElseThrow(() -> new EntityNotFoundException("Account not found"));

		// Enforce ownership
		if (!account.isOwnedBy(command.requesterId())) {
			throw new SecurityException("You are not authorized to deposit into this account");
		}

		// Create and Save PENDING Ledger Record
		TransactionRecord pendingTx = TransactionRecord.createNew(null, // Deposits have no source
				account.getAccountNumber(), // Target is this account
				command.amount(), TransactionType.DEPOSIT);
		transactionRecordRepository.save(pendingTx);

		// Publish TransactionPendingEvent
		eventPublisher.publish(new TransactionPendingEvent(UUID.randomUUID(), pendingTx.getId(), Instant.now(), null,
				account.getAccountNumber(), command.amount(), TransactionType.DEPOSIT, command.requesterId()));

		return pendingTx;
	}
}
