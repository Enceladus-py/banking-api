package com.fintech.banking.coreapi.transaction.application.service;

import java.time.Instant;
import java.util.UUID;

import com.fintech.banking.coreapi.account.application.port.in.AccountOperationsPort;
import com.fintech.banking.coreapi.common.application.annotation.TransactionalUseCase;
import com.fintech.banking.coreapi.common.domain.exception.EntityNotFoundException;
import com.fintech.banking.coreapi.transaction.application.port.in.DepositMoneyUseCase;
import com.fintech.banking.coreapi.transaction.application.port.out.EventPublisher;
import com.fintech.banking.coreapi.transaction.application.port.out.TransactionRecordRepository;
import com.fintech.banking.coreapi.transaction.domain.event.TransactionPendingEvent;
import com.fintech.banking.coreapi.transaction.domain.model.TransactionRecord;
import com.fintech.banking.coreapi.transaction.domain.model.TransactionRecord.TransactionType;

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
		// Fetch the account (No lock needed for initiating the pending state) and
		// enforce ownership
		accountOperationsPort.findByAccountNumber(command.accountId())
				.orElseThrow(() -> new EntityNotFoundException("Account not found"))
				.verifyOwnership(command.requesterId(), "You are not authorized to deposit into this account");

		// Create and Save PENDING Ledger Record
		TransactionRecord pendingTx = TransactionRecord.createNew(null, // Deposits have no source
				command.accountId(), // Target is this account
				command.amount(), TransactionType.DEPOSIT);
		transactionRecordRepository.save(pendingTx);

		// Publish TransactionPendingEvent
		eventPublisher.publish(new TransactionPendingEvent(UUID.randomUUID(), pendingTx.getId(), Instant.now(), null,
				command.accountId(), command.amount(), TransactionType.DEPOSIT, command.requesterId()));

		return pendingTx;
	}
}
