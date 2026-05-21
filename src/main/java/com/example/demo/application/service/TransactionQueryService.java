package com.example.demo.application.service;

import com.example.demo.application.port.in.GetAccountTransactionsUseCase;
import com.example.demo.application.port.in.dto.PageRequest;
import com.example.demo.application.port.in.dto.PageResult;
import com.example.demo.application.port.out.TransactionRecordRepository;
import com.example.demo.domain.model.TransactionRecord;

public class TransactionQueryService implements GetAccountTransactionsUseCase {

    private final TransactionRecordRepository transactionRecordRepository;

    public TransactionQueryService(TransactionRecordRepository transactionRecordRepository) {
        this.transactionRecordRepository = transactionRecordRepository;
    }

    @Override
    public PageResult<TransactionRecord> getTransactions(String accountNumber, PageRequest pageRequest) {
        return transactionRecordRepository.findByAccountNumber(accountNumber, pageRequest);
    }
}