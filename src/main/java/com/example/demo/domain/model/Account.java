package com.example.demo.domain.model;

import com.example.demo.domain.exception.InsufficientFundsException;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
public class Account {
    private String id;
    private String ownerId;
    private String accountNumber;
    private BigDecimal balance;
    private final Long version;

    // Creation Factory Method (contains all business validations)
    public static Account createNew(String ownerId, String accountNumber) {
        if (accountNumber == null || accountNumber.length() != 10) {
            throw new IllegalArgumentException("Account number must be exactly 10 characters");
        }
        if (ownerId == null || ownerId.trim().isBlank()) {
            throw new IllegalArgumentException("Owner ID cannot be blank");
        }
        return new Account(java.util.UUID.randomUUID().toString(), ownerId, accountNumber, BigDecimal.ZERO, null);
    }

    // The Master Constructor: Used for DB Re-hydration AND guarded new creation
    public Account(String id, String ownerId, String accountNumber, BigDecimal balance, Long version) {
        this.id = id;
        this.ownerId = ownerId;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.version = version;
    }

    public boolean isOwnedBy(String userId) {
        return this.ownerId.equals(userId);
    }

    public void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero");
        }
        this.balance = this.balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than zero");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }
        this.balance = this.balance.subtract(amount);
    }
}