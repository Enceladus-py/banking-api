package com.fintech.banking.coreapi.transaction.application.service;

import java.time.Instant;
import java.util.UUID;

import com.fintech.banking.coreapi.account.application.port.in.AccountOperationsPort;
import com.fintech.banking.coreapi.account.domain.model.Account;
import com.fintech.banking.coreapi.common.application.annotation.TransactionalUseCase;
import com.fintech.banking.coreapi.common.domain.exception.AccessDeniedException;
import com.fintech.banking.coreapi.common.domain.exception.EntityNotFoundException;
import com.fintech.banking.coreapi.transaction.application.port.in.WithdrawMoneyUseCase;
import com.fintech.banking.coreapi.transaction.application.port.out.EventPublisher;
import com.fintech.banking.coreapi.transaction.application.port.out.TransactionRecordRepository;
import com.fintech.banking.coreapi.transaction.domain.event.TransactionPendingEvent;
import com.fintech.banking.coreapi.transaction.domain.model.TransactionRecord;
import com.fintech.banking.coreapi.transaction.domain.model.TransactionRecord.TransactionType;

/**
 * Service class implementing the withdraw money use case.
 */
@TransactionalUseCase
public class WithdrawMoneyService implements WithdrawMoneyUseCase {

	private final AccountOperationsPort accountOperationsPort;
	private final TransactionRecordRepository transactionRecordRepository;
	private final EventPublisher eventPublisher;

	/**
	 * Constructs a new WithdrawMoneyService with the required ports.
	 *
	 * @param accountOperationsPort
	 *            the port for account queries and operations
	 * @param transactionRecordRepository
	 *            the repository for storing transaction ledger records
	 * @param eventPublisher
	 *            the publisher for transaction events
	 */
	public WithdrawMoneyService(AccountOperationsPort accountOperationsPort,
			TransactionRecordRepository transactionRecordRepository, EventPublisher eventPublisher) {
		this.accountOperationsPort = accountOperationsPort;
		this.transactionRecordRepository = transactionRecordRepository;
		this.eventPublisher = eventPublisher;
	}

	@Override
	public TransactionRecord withdraw(WithdrawCommand command) {
		// Fetch the account (No lock needed for initiating the pending state)
		Account account = accountOperationsPort.findByAccountNumber(command.accountId())
				.orElseThrow(() -> new EntityNotFoundException("Account not found"));

		// Enforce ownership
		if (!account.isOwnedBy(command.requesterId())) {
			throw new AccessDeniedException("You are not authorized to withdraw from this account");
		}

		// Create and Save PENDING Ledger Record
		TransactionRecord pendingTx = TransactionRecord.createNew(account.getAccountNumber(), // Source is this account
				null, // Withdrawals have no target
				command.amount(), TransactionType.WITHDRAWAL);
		transactionRecordRepository.save(pendingTx);

		// Publish TransactionPendingEvent
		eventPublisher.publish(new TransactionPendingEvent(UUID.randomUUID(), pendingTx.getId(), Instant.now(),
				account.getAccountNumber(), null, command.amount(), TransactionType.WITHDRAWAL, command.requesterId()));

		return pendingTx;
	}
}
