package com.example.demo.transaction.application.service;

import com.example.demo.account.application.port.in.AccountOperationsPort;
import com.example.demo.account.domain.model.Account;
import com.example.demo.common.application.annotation.TransactionalUseCase;
import com.example.demo.common.domain.exception.EntityNotFoundException;
import com.example.demo.transaction.application.port.in.ProcessTransactionUseCase;
import com.example.demo.transaction.application.port.out.TransactionRecordRepository;
import com.example.demo.transaction.domain.event.TransactionPendingEvent;
import com.example.demo.transaction.domain.model.TransactionRecord;
import com.example.demo.transaction.domain.model.TransactionRecord.TransactionStatus;
import com.example.demo.transaction.domain.model.TransactionRecord.TransactionType;

import lombok.extern.slf4j.Slf4j;

/**
 * Service class that implements transaction processing logic.
 */
@Slf4j
@TransactionalUseCase
public class ProcessTransactionService implements ProcessTransactionUseCase {

	private final AccountOperationsPort accountOperationsPort;
	private final TransactionRecordRepository transactionRecordRepository;

	/**
	 * Constructs a new ProcessTransactionService with the specified ports.
	 *
	 * @param accountOperationsPort
	 *            the port for account queries and operations
	 * @param transactionRecordRepository
	 *            the repository for transaction ledger records
	 */
	public ProcessTransactionService(AccountOperationsPort accountOperationsPort,
			TransactionRecordRepository transactionRecordRepository) {
		this.accountOperationsPort = accountOperationsPort;
		this.transactionRecordRepository = transactionRecordRepository;
	}

	@Override
	public void process(TransactionPendingEvent event) {
		log.info("Processing pending event for transaction ID: {}", event.transactionId());

		TransactionRecord tx = transactionRecordRepository.findById(event.transactionId()).orElseThrow(
				() -> new EntityNotFoundException("Transaction not found for ID: " + event.transactionId()));

		if (tx.getStatus() != TransactionStatus.PENDING) {
			log.info("Transaction {} already processed with status: {}", tx.getId(), tx.getStatus());
			return;
		}

		try {
			if (event.type() == TransactionType.DEPOSIT) {
				// Fetch with write lock for safety during state mutation
				Account target = accountOperationsPort.lockAndLoad(event.targetAccountNumber())
						.orElseThrow(() -> new EntityNotFoundException("Target account not found"));

				target.deposit(event.amount());
				accountOperationsPort.save(target);

			} else if (event.type() == TransactionType.WITHDRAWAL) {
				// Fetch with write lock for safety during state mutation
				Account source = accountOperationsPort.lockAndLoad(event.sourceAccountNumber())
						.orElseThrow(() -> new EntityNotFoundException("Source account not found"));

				source.withdraw(event.amount());
				accountOperationsPort.save(source);

			} else {
				// TRANSFER
				// Lock both accounts in a consistent alphabetical order to prevent deadlocks
				boolean sourceFirst = event.sourceAccountNumber().compareTo(event.targetAccountNumber()) <= 0;
				final String firstKey = sourceFirst ? event.sourceAccountNumber() : event.targetAccountNumber();
				final String secondKey = sourceFirst ? event.targetAccountNumber() : event.sourceAccountNumber();

				Account a = accountOperationsPort.lockAndLoad(firstKey)
						.orElseThrow(() -> new EntityNotFoundException("Account not found: " + firstKey));
				Account b = accountOperationsPort.lockAndLoad(secondKey)
						.orElseThrow(() -> new EntityNotFoundException("Account not found: " + secondKey));

				// Re-assign source/target after ordering
				Account source = a.getAccountNumber().equals(event.sourceAccountNumber()) ? a : b;
				Account target = a.getAccountNumber().equals(event.targetAccountNumber()) ? a : b;

				source.withdraw(event.amount());
				target.deposit(event.amount());
				accountOperationsPort.save(source);
				accountOperationsPort.save(target);
			}

			// Transition domain object to COMPLETED — no manual reconstruction needed
			transactionRecordRepository.save(tx.complete());
			log.info("Transaction {} successfully completed", tx.getId());

		} catch (com.example.demo.common.domain.exception.DomainException e) {
			// Business rule violations (Insufficient Funds, Account Not Found).
			// These are terminal. We mark the transaction as FAILED permanently.
			log.error("Transaction {} failed due to business rule: {}", tx.getId(), e.getMessage());
			transactionRecordRepository.save(tx.fail(e.getMessage()));

			// Note: We do NOT rethrow. By returning normally, the Outbox Scheduler
			// will mark the event as PUBLISHED, preventing useless retries.
		} catch (Exception e) {
			// Technical errors (Database lock timeouts, connection drops).
			// We MUST rethrow these so the @Transactional rolls back, and the
			// OutboxEventScheduler catches it to increment the retry count!
			log.error("Transaction {} failed due to technical error. Will retry. Error: {}", tx.getId(),
					e.getMessage());
			throw e;
		}
	}
}
