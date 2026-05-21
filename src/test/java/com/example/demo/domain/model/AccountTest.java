package com.example.demo.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void shouldCreateNewAccountWithZeroBalanceAndGeneratedNumber() {
        // Act
        Account account = new Account("Berat", "Dalsuna", "1234567890");

        // Assert
        assertEquals("Berat", account.getName());
        assertEquals("Dalsuna", account.getSurname());
        assertEquals(BigDecimal.ZERO, account.getBalance());

        assertNotNull(account.getAccountNumber());
        assertEquals(10, account.getAccountNumber().length());
    }

    @Test
    void shouldTrimWhitespaceFromNameAndSurname() {
        // Act: Pass in strings with leading and trailing spaces
        Account account = new Account("  Berat  ", "   Dalsuna ", "1234567890");

        // Assert: The Domain Model should have trimmed them automatically
        assertEquals("Berat", account.getName());
        assertEquals("Dalsuna", account.getSurname());
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlankOrOnlySpaces() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Account("   ", "Dalsuna", "1234567890"); // Passing only spaces
        });
        assertEquals("Name cannot be blank", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenSurnameIsBlankOrOnlySpaces() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Account("Berat", "   ", "1234567890"); // Passing only spaces
        });
        assertEquals("Surname cannot be blank", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenAccountNumberIsInvalid() {
        // Test null
        Exception nullException = assertThrows(IllegalArgumentException.class, () -> {
            new Account("Berat", "Dalsuna", null);
        });
        assertEquals("Account number must be exactly 10 characters", nullException.getMessage());

        // Test too short
        Exception shortException = assertThrows(IllegalArgumentException.class, () -> {
            new Account("Berat", "Dalsuna", "SHORT");
        });
        assertEquals("Account number must be exactly 10 characters", shortException.getMessage());

        // Test too long
        Exception longException = assertThrows(IllegalArgumentException.class, () -> {
            new Account("Berat", "Dalsuna", "WAYTOOLONGNUMBER");
        });
        assertEquals("Account number must be exactly 10 characters", longException.getMessage());
    }

    @Test
    void shouldDepositMoneySuccessfully() {
        // Arrange
        Account account = new Account("Berat", "Dalsuna", "A1B2C3D4E5");

        // Act
        account.deposit(new BigDecimal("100.50"));

        // Assert
        assertEquals(new BigDecimal("100.50"), account.getBalance());
    }

    @Test
    void shouldThrowExceptionWhenDepositingNegativeOrZeroAmount() {
        Account account = new Account("Berat", "Dalsuna", "A1B2C3D4E5");

        Exception zeroException = assertThrows(IllegalArgumentException.class, () -> {
            account.deposit(BigDecimal.ZERO);
        });
        assertEquals("Deposit amount must be greater than zero", zeroException.getMessage());

        Exception negativeException = assertThrows(IllegalArgumentException.class, () -> {
            account.deposit(new BigDecimal("-50.00"));
        });
        assertEquals("Deposit amount must be greater than zero", negativeException.getMessage());
    }

    @Test
    void shouldWithdrawMoneySuccessfully() {
        // Arrange
        Account account = new Account("Berat", "Dalsuna", "A1B2C3D4E5");
        account.deposit(new BigDecimal("200.00"));

        // Act
        account.withdraw(new BigDecimal("50.00"));

        // Assert
        assertEquals(new BigDecimal("150.00"), account.getBalance());
    }

    @Test
    void shouldThrowExceptionWhenWithdrawingMoreThanBalance() {
        Account account = new Account("Berat", "Dalsuna", "A1B2C3D4E5");
        account.deposit(new BigDecimal("100.00"));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            account.withdraw(new BigDecimal("150.00")); // Trying to overdraft
        });
        assertEquals("Insufficient funds", exception.getMessage());
        
        // Ensure balance wasn't changed
        assertEquals(new BigDecimal("100.00"), account.getBalance()); 
    }

    @Test
    void shouldThrowExceptionWhenWithdrawingNegativeOrZeroAmount() {
        Account account = new Account("Berat", "Dalsuna", "A1B2C3D4E5");
        account.deposit(new BigDecimal("100.00"));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            account.withdraw(new BigDecimal("-20.00"));
        });
        assertEquals("Withdrawal amount must be greater than zero", exception.getMessage());
    }
}