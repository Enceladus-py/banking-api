package com.example.demo.domain.model;

import lombok.Getter;
import java.math.BigDecimal;

@Getter
public class Account {
    private String id;
    private String name;
    private String surname;
    private String accountNumber;
    private BigDecimal balance;

    // For creating BRAND NEW accounts (Business Layer call)
    public Account(String name, String surname, String accountNumber) {
        // Chain to the main constructor, generating a fresh UUID and starting at 0
        this(java.util.UUID.randomUUID().toString(), name, surname, accountNumber, BigDecimal.ZERO);
    }

    // The Master Constructor: Used for DB Re-hydration AND guarded new creation
    public Account(String id, String name, String surname, String accountNumber, BigDecimal balance) {
        if (name == null || name.trim().isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
        if (surname == null || surname.trim().isBlank()) {
            throw new IllegalArgumentException("Surname cannot be blank");
        }
        if (accountNumber == null || accountNumber.length() != 10) {
            throw new IllegalArgumentException("Account number must be exactly 10 characters");
        }

        this.id = id;
        this.name = name.trim(); // Trimming happens safely for both paths
        this.surname = surname.trim(); // Trimming happens safely for both paths
        this.accountNumber = accountNumber;
        this.balance = balance;
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
            throw new IllegalStateException("Insufficient funds");
        }
        this.balance = this.balance.subtract(amount);
    }
}