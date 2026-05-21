package com.example.demo.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Getter;

@Getter
public class TransactionRecord {

    private final String id;
    private final String sourceAccountNumber; // Can be null for direct deposits
    private final String targetAccountNumber; // Can be null for direct withdrawals
    private final BigDecimal amount;
    private final TransactionType type;
    private final LocalDateTime timestamp;

    public enum TransactionType {
        DEPOSIT,
        WITHDRAWAL,
        TRANSFER
    }

    // Constructor for creating a BRAND NEW transaction (Business Layer)
    public TransactionRecord(String sourceAccountNumber, String targetAccountNumber, BigDecimal amount,
            TransactionType type) {
        this(UUID.randomUUID().toString(), sourceAccountNumber, targetAccountNumber, amount, type, LocalDateTime.now());
    }

    // Main Constructor (Data mapping & Validation)
    public TransactionRecord(String id, String sourceAccountNumber, String targetAccountNumber, BigDecimal amount,
            TransactionType type, LocalDateTime timestamp) {
        // 1. Amount validations
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be strictly positive");
        }

        // 2. Type validation
        if (type == null) {
            throw new IllegalArgumentException("Transaction type cannot be null");
        }

        // 3. Account participation validations
        if (sourceAccountNumber == null && targetAccountNumber == null) {
            throw new IllegalArgumentException("Source and target accounts cannot both be null");
        }

        // 4. Specific rules for types
        switch (type) {
            case DEPOSIT -> {
                if (sourceAccountNumber != null)
                    throw new IllegalArgumentException("Deposits cannot have a source account");
                if (targetAccountNumber == null)
                    throw new IllegalArgumentException("Deposits must specify a target account");
            }
            case WITHDRAWAL -> {
                if (sourceAccountNumber == null)
                    throw new IllegalArgumentException("Withdrawals must specify a source account");
                if (targetAccountNumber != null)
                    throw new IllegalArgumentException("Withdrawals cannot have a target account");
            }
            case TRANSFER -> {
                if (sourceAccountNumber == null || targetAccountNumber == null) {
                    throw new IllegalArgumentException("Transfers must specify both source and target accounts");
                }
                if (sourceAccountNumber.equals(targetAccountNumber)) {
                    throw new IllegalArgumentException("Cannot transfer money to the same account");
                }
            }
        }

        this.id = id;
        this.sourceAccountNumber = sourceAccountNumber;
        this.targetAccountNumber = targetAccountNumber;
        this.amount = amount;
        this.type = type;
        this.timestamp = timestamp;
    }

}