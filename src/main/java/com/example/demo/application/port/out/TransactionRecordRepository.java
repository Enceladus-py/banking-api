package com.example.demo.application.port.out;

import com.example.demo.domain.model.TransactionRecord;
import java.util.List;

public interface TransactionRecordRepository {
    // We don't need to return the record here since it's an append-only ledger
    void save(TransactionRecord transaction);

    // Fetch the history for an account (whether money went IN or OUT)
    List<TransactionRecord> findByAccountNumber(String accountNumber);
}