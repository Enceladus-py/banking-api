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
        assertEquals(10, account.getAccountNumber().length(), "Account number should be 10 characters");
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Account("", "Dalsuna");
        });
        assertEquals("Name cannot be blank", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenSurnameIsBlank() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            new Account("Berat", null);
        });
        assertEquals("Surname cannot be blank", exception.getMessage());
    }
}