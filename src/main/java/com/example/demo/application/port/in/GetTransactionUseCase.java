package com.example.demo.application.port.in;

import com.example.demo.domain.model.TransactionRecord;

public interface GetTransactionUseCase {
    TransactionRecord getTransaction(String transactionId, String requesterId);
}
