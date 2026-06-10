package com.fintech.banking.coreapi.transaction.application.port.in;

import com.fintech.banking.coreapi.transaction.domain.model.TransactionRecord;

/**
 * Inbound port interface for retrieving single transaction record details.
 */
public interface GetTransactionUseCase {
	/**
	 * Retrieves details of a specific transaction by its identifier.
	 *
	 * @param transactionId
	 *            the unique transaction ID
	 * @param requesterId
	 *            the user ID requesting transaction details
	 * @return the transaction record details
	 */
	TransactionRecord getTransaction(String transactionId, String requesterId);
}
