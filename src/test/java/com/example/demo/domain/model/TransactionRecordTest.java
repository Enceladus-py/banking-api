package com.example.demo.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class TransactionRecordTest {

    @Test
    void shouldCreateValidDepositRecord() {
        TransactionRecord record = new TransactionRecord(
                null,
                "TARGET1234",
                new BigDecimal("100.00"),
                TransactionRecord.TransactionType.DEPOSIT);

        assertNull(record.getSourceAccountNumber());
        assertEquals("TARGET1234", record.getTargetAccountNumber());
        assertEquals(TransactionRecord.TransactionType.DEPOSIT, record.getType());
    }

    @Test
    void shouldThrowExceptionWhenDepositHasSourceAccount() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            new TransactionRecord(
                    "SOURCE1234",
                    "TARGET1234",
                    new BigDecimal("100.00"),
                    TransactionRecord.TransactionType.DEPOSIT);
        });
        assertEquals("Deposits cannot have a source account", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenWithdrawalHasTargetAccount() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            new TransactionRecord(
                    "SOURCE1234",
                    "TARGET1234",
                    new BigDecimal("100.00"),
                    TransactionRecord.TransactionType.WITHDRAWAL);
        });
        assertEquals("Withdrawals cannot have a target account", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenTransferHasSameSourceAndTarget() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            new TransactionRecord(
                    "SAME123456",
                    "SAME123456",
                    new BigDecimal("100.00"),
                    TransactionRecord.TransactionType.TRANSFER);
        });
        assertEquals("Cannot transfer money to the same account", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionForZeroOrNegativeAmount() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            new TransactionRecord(
                    null,
                    "TARGET1234",
                    BigDecimal.ZERO,
                    TransactionRecord.TransactionType.DEPOSIT);
        });
        assertEquals("Transaction amount must be strictly positive", ex.getMessage());
    }
}