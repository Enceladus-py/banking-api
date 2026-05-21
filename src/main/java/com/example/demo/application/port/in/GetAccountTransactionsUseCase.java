package com.example.demo.application.port.in;

import com.example.demo.application.port.in.dto.PageRequest;
import com.example.demo.application.port.in.dto.PageResult;
import com.example.demo.domain.model.TransactionRecord;

public interface GetAccountTransactionsUseCase {
    PageResult<TransactionRecord> getTransactions(String accountNumber, PageRequest pageRequest);
}