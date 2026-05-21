package com.example.demo.application.service;

import com.example.demo.application.port.in.CreateAccountUseCase.CreateAccountCommand;
import com.example.demo.application.port.in.DepositMoneyUseCase.DepositCommand;
import com.example.demo.application.port.in.WithdrawMoneyUseCase.WithdrawCommand;
import com.example.demo.application.port.out.AccountRepository;
import com.example.demo.domain.model.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankAccountServiceTest {

    @Mock
    private AccountRepository accountRepository; // Fake the Outbound Port

    private BankAccountService bankAccountService;

    @BeforeEach
    void setUp() {
        // Inject the fake repository into our real service
        bankAccountService = new BankAccountService(accountRepository);
    }

    @Test
    void shouldCreateAccountAndSaveToRepository() {
        // 1. Arrange
        CreateAccountCommand command = new CreateAccountCommand("Berat", "Dalsuna");

        // When the fake repo is asked to save ANY Account, return this specific one
        Account mockSavedAccount = new Account("uuid-123", "Berat", "Dalsuna", "A1B2C3D4E5", BigDecimal.ZERO);
        when(accountRepository.save(any(Account.class))).thenReturn(mockSavedAccount);

        // 2. Act
        Account result = bankAccountService.createAccount(command);

        // 3. Assert
        assertEquals("uuid-123", result.getId());

        // Use an ArgumentCaptor to intercept the EXACT Account object passed to the
        // repository
        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accountCaptor.capture());

        Account capturedAccount = accountCaptor.getValue();
        assertEquals("Berat", capturedAccount.getName());
        assertEquals("Dalsuna", capturedAccount.getSurname());
        assertEquals(BigDecimal.ZERO, capturedAccount.getBalance());
    }

    @Test
    void shouldRetryGeneratingNumberWhenFirstNumberAlreadyExists() {
        // Arrange
        CreateAccountCommand command = new CreateAccountCommand("Berat", "Dalsuna");
        Account account = new Account("Berat", "Dalsuna", "A1B2C3D4E5");

        // Mock the uniqueness check:
        // 1st call returns TRUE (collision!), 2nd call returns FALSE (free!)
        when(accountRepository.findByAccountNumber(anyString()))
                .thenReturn(Optional.of(account))
                .thenReturn(Optional.empty());

        // Mock the save operation
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Account result = bankAccountService.createAccount(command);

        // Assert
        assertNotNull(result.getAccountNumber());
        assertEquals(10, result.getAccountNumber().length());

        // **The crucial assertion:** Verify the do-while loop actually ran twice!
        verify(accountRepository, times(2)).findByAccountNumber(anyString());

        // Verify it still only saved once
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void shouldSuccessfullyDepositMoneyAndSave() {
        // Arrange
        String accountNumber = "A1B2C3D4E5";
        DepositCommand command = new DepositCommand(accountNumber, new BigDecimal("250.00"));

        Account existingAccount = new Account("uuid-123", "Berat", "Dalsuna", accountNumber, BigDecimal.ZERO);

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Account updatedAccount = bankAccountService.deposit(command);

        // Assert
        assertNotNull(updatedAccount);
        assertEquals(new BigDecimal("250.00"), updatedAccount.getBalance());

        verify(accountRepository, times(1)).findByAccountNumber(accountNumber);
        verify(accountRepository, times(1)).save(existingAccount);
    }

    @Test
    void shouldThrowExceptionWhenAccountDoesNotExist() {
        // Arrange
        String nonExistentAccountNumber = "NOTFOUND12";
        DepositCommand command = new DepositCommand(nonExistentAccountNumber, new BigDecimal("100.00"));

        when(accountRepository.findByAccountNumber(nonExistentAccountNumber)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bankAccountService.deposit(command);
        });

        assertEquals("Account not found", exception.getMessage());
        verify(accountRepository, times(1)).findByAccountNumber(nonExistentAccountNumber);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void shouldDepositMoneyAndSaveSuccessfully() {
        // Arrange
        String accountNumber = "1234567890";
        // Start with $500 balance
        Account existingAccount = new Account("uuid-1", "Berat", "Dalsuna", accountNumber, new BigDecimal("500.00"));
        DepositCommand command = new DepositCommand(accountNumber, new BigDecimal("250.00"));

        // Mock repository behavior
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Account updatedAccount = bankAccountService.deposit(command);

        // Assert
        assertEquals(new BigDecimal("750.00"), updatedAccount.getBalance());

        // Verify orchestration order
        verify(accountRepository).findByAccountNumber(accountNumber);
        verify(accountRepository).save(existingAccount);
    }

    @Test
    void shouldThrowExceptionWhenAccountNotFoundForDeposit() {
        // Arrange
        String accountNumber = "0000000000";
        DepositCommand command = new DepositCommand(accountNumber, new BigDecimal("100.00"));

        // Simulate database returning empty
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bankAccountService.deposit(command);
        });

        assertEquals("Account not found", exception.getMessage());

        // Crucial: Ensure we NEVER attempt to save corrupted/null state back to the DB
        verify(accountRepository).findByAccountNumber(accountNumber);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void shouldWithdrawMoneyAndSaveSuccessfully() {
        // Arrange
        String accountNumber = "1234567890";
        Account existingAccount = new Account("uuid-1", "Berat", "Dalsuna", accountNumber, new BigDecimal("500.00"));
        WithdrawCommand command = new WithdrawCommand(accountNumber, new BigDecimal("150.00"));

        // Mock the repository to return the account, and just return whatever is passed
        // to save()
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Account updatedAccount = bankAccountService.withdraw(command);

        // Assert
        assertEquals(new BigDecimal("350.00"), updatedAccount.getBalance());

        // Verify the orchestrator called the port methods in the correct order
        verify(accountRepository).findByAccountNumber(accountNumber);
        verify(accountRepository).save(existingAccount);
    }

    @Test
    void shouldThrowExceptionWhenAccountNotFoundForWithdrawal() {
        // Arrange
        String accountNumber = "0000000000";
        WithdrawCommand command = new WithdrawCommand(accountNumber, new BigDecimal("100.00"));

        // Simulate database returning empty
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bankAccountService.withdraw(command);
        });

        assertEquals("Account not found", exception.getMessage());

        // Crucial: Ensure we NEVER attempt to save if the account wasn't found
        verify(accountRepository).findByAccountNumber(accountNumber);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void shouldAbortTransactionWhenDomainThrowsInsufficientFunds() {
        // Arrange
        String accountNumber = "1234567890";
        Account existingAccount = new Account("uuid-1", "Berat", "Dalsuna", accountNumber, new BigDecimal("50.00"));
        WithdrawCommand command = new WithdrawCommand(accountNumber, new BigDecimal("100.00"));

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(existingAccount));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            bankAccountService.withdraw(command);
        });

        assertEquals("Insufficient funds", exception.getMessage());

        // Crucial: Ensure the service respects the domain exception and ABORTS the save
        // operation
        verify(accountRepository).findByAccountNumber(accountNumber);
        verify(accountRepository, never()).save(any());
    }
}