package com.example.demo.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Test
    void shouldCreateAccountWithGeneratedIdAndZeroBalance() {
        Account account = Account.createNew("USER-123", "ACC-999999");

        assertNotNull(account.getId());
        assertEquals("USER-123", account.getOwnerId());
        assertEquals("ACC-999999", account.getAccountNumber());
        assertEquals(BigDecimal.ZERO, account.getBalance());
    }

    @Test
    void shouldThrowExceptionWhenOwnerIdOrAccountNumberIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> Account.createNew("  ", "ACC-999"));
        assertThrows(IllegalArgumentException.class, () -> Account.createNew("USER-123", "  "));
    }

    @Test
    void shouldValidateOwnership() {
        Account account = Account.createNew("USER-123", "ACC-999999");
        assertTrue(account.isOwnedBy("USER-123"));
        assertFalse(account.isOwnedBy("HACKER-999999"));
    }

    @Test
    void shouldDepositMoneySuccessfully() {
        Account account = Account.createNew("USER-1", "ACC-123456");
        account.deposit(new BigDecimal("100.50"));
        assertEquals(new BigDecimal("100.50"), account.getBalance());
    }

    @Test
    void shouldWithdrawMoneySuccessfully() {
        Account account = new Account("ID", "USER-1", "ACC-123456", new BigDecimal("100.00"), false);
        account.withdraw(new BigDecimal("40.00"));
        assertEquals(new BigDecimal("60.00"), account.getBalance());
    }

    @Test
    void shouldThrowExceptionWhenInsufficientFunds() {
        Account account = new Account("ID", "USER-1", "ACC-123456", new BigDecimal("50.00"), false);
        assertThrows(IllegalStateException.class, () -> account.withdraw(new BigDecimal("100.00")));
    }

    @Test
    void shouldCreateAccountUsingStaticFactoryMethodAndBeMarkedAsNew() {
        Account account = Account.createNew("USER-123", "ACC-999999");
        assertNotNull(account.getId());
        assertEquals("USER-123", account.getOwnerId());
        assertEquals("ACC-999999", account.getAccountNumber());
        assertEquals(BigDecimal.ZERO, account.getBalance());
        assertTrue(account.isNew());
    }

    @Test
    void shouldCreateRehydratedAccountWithMasterConstructorAndNotBeMarkedAsNew() {
        Account account = new Account("ID-123", "USER-123", "ACC-999999", new BigDecimal("250.00"), false);
        assertEquals("ID-123", account.getId());
        assertEquals("USER-123", account.getOwnerId());
        assertEquals("ACC-999999", account.getAccountNumber());
        assertEquals(new BigDecimal("250.00"), account.getBalance());
        assertFalse(account.isNew());
    }
}