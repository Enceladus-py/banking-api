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

    @Test
    void shouldThrowExceptionForNullAmount() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            new TransactionRecord(
                    null,
                    "TARGET1234",
                    null,
                    TransactionRecord.TransactionType.DEPOSIT);
        });
        assertEquals("Transaction amount must be strictly positive", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionForNullType() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            new TransactionRecord(
                    null,
                    "TARGET1234",
                    new BigDecimal("100.00"),
                    null);
        });
        assertEquals("Transaction type cannot be null", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenSourceAndTargetAreBothNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            new TransactionRecord(
                    null,
                    null,
                    new BigDecimal("100.00"),
                    TransactionRecord.TransactionType.TRANSFER);
        });
        assertEquals("Source and target accounts cannot both be null", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenWithdrawalMissingSourceAccount() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            new TransactionRecord(
                    null,
                    "TARGET1234",
                    new BigDecimal("100.00"),
                    TransactionRecord.TransactionType.WITHDRAWAL);
        });
        assertEquals("Withdrawals must specify a source account", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenTransferMissingTargetAccount() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            new TransactionRecord(
                    "SOURCE1234",
                    null,
                    new BigDecimal("100.00"),
                    TransactionRecord.TransactionType.TRANSFER);
        });
        assertEquals("Transfers must specify both source and target accounts", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenTransferMissingSourceAccount() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            new TransactionRecord(
                    null,
                    "TARGET1234",
                    new BigDecimal("100.00"),
                    TransactionRecord.TransactionType.TRANSFER);
        });
        assertEquals("Transfers must specify both source and target accounts", ex.getMessage());
    }

    @Test
    void shouldCompleteTransaction() {
        TransactionRecord record = new TransactionRecord(
                null,
                "TARGET1234",
                new BigDecimal("100.00"),
                TransactionRecord.TransactionType.DEPOSIT);
        
        TransactionRecord completed = record.complete();
        
        assertEquals(TransactionRecord.TransactionStatus.COMPLETED, completed.getStatus());
        assertEquals(record.getId(), completed.getId());
    }

    @Test
    void shouldFailTransaction() {
        TransactionRecord record = new TransactionRecord(
                null,
                "TARGET1234",
                new BigDecimal("100.00"),
                TransactionRecord.TransactionType.DEPOSIT);
        
        TransactionRecord failed = record.fail("Some error");
        
        assertEquals(TransactionRecord.TransactionStatus.FAILED, failed.getStatus());
        assertEquals("Some error", failed.getFailureReason());
        assertEquals(record.getId(), failed.getId());
    }

    @Test
    void shouldThrowExceptionWhenCompletingAlreadyCompletedTransaction() {
        TransactionRecord record = new TransactionRecord(
                null,
                "TARGET1234",
                new BigDecimal("100.00"),
                TransactionRecord.TransactionType.DEPOSIT).complete();
        
        IllegalStateException ex = assertThrows(IllegalStateException.class, record::complete);
        assertEquals("Only PENDING transactions can be completed, but current status is COMPLETED", ex.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenFailingAlreadyCompletedTransaction() {
        TransactionRecord record = new TransactionRecord(
                null,
                "TARGET1234",
                new BigDecimal("100.00"),
                TransactionRecord.TransactionType.DEPOSIT).complete();
        
        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> record.fail("error"));
        assertEquals("Only PENDING transactions can be failed, but current status is COMPLETED", ex.getMessage());
    }
}