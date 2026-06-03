package com.example.demo.transaction.application.port.out;

import java.util.Optional;

import com.example.demo.common.application.port.in.dto.PageRequest;
import com.example.demo.common.application.port.in.dto.PageResult;
import com.example.demo.transaction.domain.model.TransactionRecord;

/**
 * Outbound port interface for transaction record persistence.
 */
public interface TransactionRecordRepository {
	/**
	 * Saves a transaction record to the ledger.
	 *
	 * @param transaction
	 *            the transaction record to save
	 */
	void save(TransactionRecord transaction);

	/**
	 * Finds a transaction record by its unique ID.
	 *
	 * @param id
	 *            the transaction ID
	 * @return an Optional containing the transaction record if found, or empty
	 */
	Optional<TransactionRecord> findById(String id);

	/**
	 * Fetches the paginated history of transaction records involving the specified
	 * account number.
	 *
	 * @param accountNumber
	 *            the account number
	 * @param pageRequest
	 *            the pagination page request parameters
	 * @return a paginated result containing transaction records
	 */
	PageResult<TransactionRecord> findByAccountNumber(String accountNumber, PageRequest pageRequest);
}
