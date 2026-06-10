package com.fintech.banking.coreapi.transaction.application.service;

import com.fintech.banking.coreapi.account.application.port.in.AccountOperationsPort;
import com.fintech.banking.coreapi.account.domain.model.Account;
import com.fintech.banking.coreapi.common.application.annotation.UseCase;
import com.fintech.banking.coreapi.common.application.port.in.dto.PageRequest;
import com.fintech.banking.coreapi.common.application.port.in.dto.PageResult;
import com.fintech.banking.coreapi.common.domain.exception.AccessDeniedException;
import com.fintech.banking.coreapi.common.domain.exception.EntityNotFoundException;
import com.fintech.banking.coreapi.transaction.application.port.in.GetAccountTransactionsUseCase;
import com.fintech.banking.coreapi.transaction.application.port.in.GetTransactionUseCase;
import com.fintech.banking.coreapi.transaction.application.port.out.TransactionRecordRepository;
import com.fintech.banking.coreapi.transaction.domain.model.TransactionRecord;

/**
 * Service class implementing queries for transaction histories and details.
 */
@UseCase
public class TransactionQueryService implements GetAccountTransactionsUseCase, GetTransactionUseCase {

	private final TransactionRecordRepository transactionRecordRepository;
	private final AccountOperationsPort accountOperationsPort;

	/**
	 * Constructs a new TransactionQueryService with the specified ports.
	 *
	 * @param transactionRecordRepository
	 *            the transaction record repository outbound port
	 * @param accountOperationsPort
	 *            the account operations inbound port
	 */
	public TransactionQueryService(TransactionRecordRepository transactionRecordRepository,
			AccountOperationsPort accountOperationsPort) {
		this.transactionRecordRepository = transactionRecordRepository;
		this.accountOperationsPort = accountOperationsPort;
	}

	@Override
	public PageResult<TransactionRecord> getTransactions(String accountNumber, PageRequest pageRequest,
			String requesterId) {
		accountOperationsPort.findByAccountNumber(accountNumber)
				.orElseThrow(() -> new EntityNotFoundException("Account not found"))
				.verifyOwnership(requesterId, "You are not authorized to view this account's transactions");

		return transactionRecordRepository.findByAccountNumber(accountNumber, pageRequest);
	}

	@Override
	public TransactionRecord getTransaction(String transactionId, String requesterId) {
		TransactionRecord tx = transactionRecordRepository.findById(transactionId)
				.orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

		boolean ownsSource = false;
		boolean ownsTarget = false;

		if (tx.getSourceAccountNumber() != null) {
			Account source = accountOperationsPort.findByAccountNumber(tx.getSourceAccountNumber()).orElse(null);
			if (source != null && source.isOwnedBy(requesterId)) {
				ownsSource = true;
			}
		}

		if (tx.getTargetAccountNumber() != null) {
			Account target = accountOperationsPort.findByAccountNumber(tx.getTargetAccountNumber()).orElse(null);
			if (target != null && target.isOwnedBy(requesterId)) {
				ownsTarget = true;
			}
		}

		if (!ownsSource && !ownsTarget) {
			throw new AccessDeniedException("You are not authorized to view this transaction");
		}

		return tx;
	}
}
