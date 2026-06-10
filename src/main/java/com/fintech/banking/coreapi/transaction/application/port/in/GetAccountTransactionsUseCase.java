package com.fintech.banking.coreapi.transaction.application.port.in;

import com.fintech.banking.coreapi.common.application.port.in.dto.PageRequest;
import com.fintech.banking.coreapi.common.application.port.in.dto.PageResult;
import com.fintech.banking.coreapi.transaction.domain.model.TransactionRecord;

/**
 * Inbound port interface for retrieving transactions of an account with
 * pagination.
 */
public interface GetAccountTransactionsUseCase {
	/**
	 * Retrieves paginated transaction records for the given account number.
	 *
	 * @param accountNumber
	 *            the account number
	 * @param pageRequest
	 *            pagination details
	 * @param requesterId
	 *            the user ID requesting transactions history
	 * @return a paginated container of transaction records
	 */
	PageResult<TransactionRecord> getTransactions(String accountNumber, PageRequest pageRequest, String requesterId);
}
