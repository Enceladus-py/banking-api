package com.example.demo.application.port.out;

import java.util.Optional;

import com.example.demo.application.port.in.dto.PageRequest;
import com.example.demo.application.port.in.dto.PageResult;
import com.example.demo.domain.model.TransactionRecord;

public interface TransactionRecordRepository {
	// We don't need to return the record here since it's an append-only ledger
	void save(TransactionRecord transaction);

	Optional<TransactionRecord> findById(String id);

	// Fetch the history for an account (whether money went IN or OUT)
	PageResult<TransactionRecord> findByAccountNumber(String accountNumber, PageRequest pageRequest);
}
