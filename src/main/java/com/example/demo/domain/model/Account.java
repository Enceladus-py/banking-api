package com.example.demo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@AllArgsConstructor // Generates the re-hydration constructor
public class Account {
    private String id;
    private String name;
    private String surname;
    private String accountNumber;
    private BigDecimal balance;

    public Account(String name, String surname) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
        if (surname == null || surname.isBlank()) {
            throw new IllegalArgumentException("Surname cannot be blank");
        }

        this.name = name.trim();
        this.surname = surname.trim();
        this.accountNumber = UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        this.balance = BigDecimal.ZERO;
    }
}