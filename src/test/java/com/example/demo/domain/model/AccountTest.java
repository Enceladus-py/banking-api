package com.example.demo.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void shouldCreateNewAccountWithZeroBalanceAndGeneratedNumber() {
        // Act
        Account account = new Account("Berat", "Dalsuna");

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
        Account account = new Account("  Berat  ", "   Dalsuna ");

        // Assert: The Domain Model should have trimmed them automatically
        assertEquals("Berat", account.getName());
        assertEquals("Dalsuna", account.getSurname());
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlankOrOnlySpaces() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Account("   ", "Dalsuna"); // Passing only spaces
        });
        assertEquals("Account name cannot be blank", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenSurnameIsBlankOrOnlySpaces() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Account("Berat", "   "); // Passing only spaces
        });
        assertEquals("Account surname cannot be blank", exception.getMessage());
    }
}