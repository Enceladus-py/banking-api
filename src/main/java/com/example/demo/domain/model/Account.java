package com.example.demo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.math.BigDecimal;

@Getter
@AllArgsConstructor // Generates the re-hydration constructor
public class Account {
    private String id;
    private String name;
    private String surname;
    private String accountNumber;
    private BigDecimal balance;

    public Account(String name, String surname, String accountNumber) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
        if (surname == null || surname.isBlank()) {
            throw new IllegalArgumentException("Surname cannot be blank");
        }
        if (accountNumber == null || accountNumber.length() != 10) {
            throw new IllegalArgumentException("Account number must be exactly 10 characters");
        }

        this.name = name.trim();
        this.surname = surname.trim();
        this.accountNumber = accountNumber;
        this.balance = BigDecimal.ZERO;
    }
}