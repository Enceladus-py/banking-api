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
}