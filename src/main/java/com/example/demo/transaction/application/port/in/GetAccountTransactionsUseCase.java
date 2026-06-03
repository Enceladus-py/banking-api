package com.example.demo.transaction.application.port.in;

import com.example.demo.common.application.port.in.dto.PageRequest;
import com.example.demo.common.application.port.in.dto.PageResult;
import com.example.demo.transaction.domain.model.TransactionRecord;

public interface GetAccountTransactionsUseCase {
	PageResult<TransactionRecord> getTransactions(String accountNumber, PageRequest pageRequest, String requesterId);
}
