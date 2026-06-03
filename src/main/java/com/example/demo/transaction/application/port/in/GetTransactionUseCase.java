package com.example.demo.transaction.application.port.in;

import com.example.demo.transaction.domain.model.TransactionRecord;

public interface GetTransactionUseCase {
	TransactionRecord getTransaction(String transactionId, String requesterId);
}
