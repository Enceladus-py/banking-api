package com.example.demo.application.service;

import com.example.demo.application.port.in.GetAccountTransactionsUseCase;
import com.example.demo.application.port.in.GetTransactionUseCase;
import com.example.demo.application.port.in.dto.PageRequest;
import com.example.demo.application.port.in.dto.PageResult;
import com.example.demo.application.port.out.AccountRepository;
import com.example.demo.application.port.out.TransactionRecordRepository;
import com.example.demo.domain.model.Account;
import com.example.demo.domain.model.TransactionRecord;

import com.example.demo.domain.exception.EntityNotFoundException;

public class TransactionQueryService implements GetAccountTransactionsUseCase, GetTransactionUseCase {

    private final TransactionRecordRepository transactionRecordRepository;
    private final AccountRepository accountRepository;

    public TransactionQueryService(TransactionRecordRepository transactionRecordRepository,
            AccountRepository accountRepository) {
        this.transactionRecordRepository = transactionRecordRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public PageResult<TransactionRecord> getTransactions(String accountNumber, PageRequest pageRequest,
            String requesterId) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        if (!account.isOwnedBy(requesterId)) {
            throw new SecurityException("You are not authorized to view this account's transactions");
        }

        return transactionRecordRepository.findByAccountNumber(accountNumber, pageRequest);
    }

    @Override
    public TransactionRecord getTransaction(String transactionId, String requesterId) {
        TransactionRecord tx = transactionRecordRepository.findById(transactionId)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found"));

        boolean ownsSource = false;
        boolean ownsTarget = false;

        if (tx.getSourceAccountNumber() != null) {
            Account source = accountRepository.findByAccountNumber(tx.getSourceAccountNumber()).orElse(null);
            if (source != null && source.isOwnedBy(requesterId)) {
                ownsSource = true;
            }
        }

        if (tx.getTargetAccountNumber() != null) {
            Account target = accountRepository.findByAccountNumber(tx.getTargetAccountNumber()).orElse(null);
            if (target != null && target.isOwnedBy(requesterId)) {
                ownsTarget = true;
            }
        }

        if (!ownsSource && !ownsTarget) {
            throw new SecurityException("You are not authorized to view this transaction");
        }

        return tx;
    }
}