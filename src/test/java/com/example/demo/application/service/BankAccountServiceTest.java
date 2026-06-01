package com.example.demo.application.service;

import com.example.demo.application.port.in.CreateAccountUseCase.CreateAccountCommand;
import com.example.demo.application.port.in.DepositMoneyUseCase.DepositCommand;
import com.example.demo.application.port.in.WithdrawMoneyUseCase.WithdrawCommand;
import com.example.demo.application.port.out.AccountRepository;
import com.example.demo.application.port.out.TransactionRecordRepository;
import com.example.demo.application.port.out.UserRepository;
import com.example.demo.domain.model.Account;
import com.example.demo.domain.model.TransactionRecord;
import com.example.demo.domain.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import com.example.demo.domain.exception.EntityNotFoundException;
import com.example.demo.domain.exception.InsufficientFundsException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    private UserRepository userRepository; // Needed to verify users exist before creating accounts

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRecordRepository transactionRecordRepository;

    private BankAccountService bankAccountService;

    @Captor
    private ArgumentCaptor<TransactionRecord> transactionCaptor;

    @BeforeEach
    void setUp() {
        // Inject the fake repositories into our real service
        bankAccountService = new BankAccountService(accountRepository, transactionRecordRepository, userRepository);
    }

    @Test
    void shouldCreateAccountAndSaveToRepository() {
        // 1. Arrange
        String requesterId = "USER-123";
        CreateAccountCommand command = new CreateAccountCommand(requesterId);

        // Mock User exists
        when(userRepository.findById(requesterId)).thenReturn(Optional.of(new User(requesterId, "Berat", "Dalsuna", 1L)));

        // When the fake repo is asked to save ANY Account, return this specific one
        Account mockSavedAccount = new Account("uuid-123", requesterId, "A1B2C3D4E5", BigDecimal.ZERO, 1L);
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
        assertEquals(requesterId, capturedAccount.getOwnerId());
        assertNotNull(capturedAccount.getAccountNumber());
        assertEquals(BigDecimal.ZERO, capturedAccount.getBalance());
    }

    @Test
    void shouldThrowExceptionWhenCreatingAccountForNonExistentUser() {
        // Arrange
        CreateAccountCommand command = new CreateAccountCommand("GHOST-1");
        when(userRepository.findById("GHOST-1")).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bankAccountService.createAccount(command);
        });

        assertEquals("User not found", exception.getMessage());
        verify(accountRepository, never()).save(any());
    }

    @Test
    void shouldRetryGeneratingNumberWhenFirstNumberAlreadyExists() {
        // Arrange
        String requesterId = "USER-123";
        CreateAccountCommand command = new CreateAccountCommand(requesterId);
        Account account = Account.createNew(requesterId, "A1B2C3D4E5");

        when(userRepository.findById(requesterId)).thenReturn(Optional.of(new User(requesterId, "Berat", "Dalsuna", 1L)));

        // Mock the uniqueness check: 1st call returns TRUE (collision!), 2nd call
        // returns FALSE (free!)
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
        String requesterId = "USER-123";
        String accountNumber = "A1B2C3D4E5";
        DepositCommand command = new DepositCommand(accountNumber, new BigDecimal("250.00"), requesterId);

        Account existingAccount = new Account("uuid-123", requesterId, accountNumber, BigDecimal.ZERO, 1L);

        when(accountRepository.findByAccountNumberForWrite(accountNumber)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Account updatedAccount = bankAccountService.deposit(command);

        // Assert Account
        assertNotNull(updatedAccount);
        assertEquals(new BigDecimal("250.00"), updatedAccount.getBalance());

        verify(accountRepository, times(1)).findByAccountNumberForWrite(accountNumber);
        verify(accountRepository, times(1)).save(existingAccount);

        // Assert Ledger
        verify(transactionRecordRepository, times(1)).save(transactionCaptor.capture());
        TransactionRecord savedLedger = transactionCaptor.getValue();
        assertNull(savedLedger.getSourceAccountNumber());
        assertEquals(accountNumber, savedLedger.getTargetAccountNumber());
        assertEquals(new BigDecimal("250.00"), savedLedger.getAmount());
        assertEquals(TransactionRecord.TransactionType.DEPOSIT, savedLedger.getType());
    }

    @Test
    void shouldThrowSecurityExceptionWhenDepositingAsWrongUser() {
        // Arrange
        String accountNumber = "A1B2C3D4E5";
        Account existingAccount = new Account("uuid-123", "REAL-OWNER", accountNumber, BigDecimal.ZERO, 1L);
        DepositCommand command = new DepositCommand(accountNumber, new BigDecimal("100.00"), "HACKER");

        when(accountRepository.findByAccountNumberForWrite(accountNumber)).thenReturn(Optional.of(existingAccount));

        // Act & Assert
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            bankAccountService.deposit(command);
        });

        assertEquals("You are not authorized to deposit into this account", exception.getMessage());
        verify(accountRepository, never()).save(any());
        verify(transactionRecordRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenAccountNotFoundForDeposit() {
        // Arrange
        String nonExistentAccountNumber = "NOTFOUND12";
        DepositCommand command = new DepositCommand(nonExistentAccountNumber, new BigDecimal("100.00"), "USER-1");

        when(accountRepository.findByAccountNumberForWrite(nonExistentAccountNumber)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bankAccountService.deposit(command);
        });

        assertEquals("Account not found", exception.getMessage());

        // Crucial: Ensure we NEVER attempt to save corrupted/null state back to the DB
        verify(accountRepository, times(1)).findByAccountNumberForWrite(nonExistentAccountNumber);
        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRecordRepository, never()).save(any());
    }

    @Test
    void shouldWithdrawMoneyAndSaveSuccessfully() {
        // Arrange
        String requesterId = "USER-123";
        String accountNumber = "1234567890";
        Account existingAccount = new Account("uuid-1", requesterId, accountNumber, new BigDecimal("500.00"), 1L);
        WithdrawCommand command = new WithdrawCommand(accountNumber, new BigDecimal("150.00"), requesterId);

        // Mock the repository to return the account, and just return whatever is passed
        // to save()
        when(accountRepository.findByAccountNumberForWrite(accountNumber)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Account updatedAccount = bankAccountService.withdraw(command);

        // Assert Account
        assertEquals(new BigDecimal("350.00"), updatedAccount.getBalance());

        // Verify the orchestrator called the port methods in the correct order
        verify(accountRepository).findByAccountNumberForWrite(accountNumber);
        verify(accountRepository).save(existingAccount);

        // Assert Ledger
        verify(transactionRecordRepository).save(transactionCaptor.capture());
        TransactionRecord savedLedger = transactionCaptor.getValue();
        assertEquals(accountNumber, savedLedger.getSourceAccountNumber());
        assertNull(savedLedger.getTargetAccountNumber());
        assertEquals(new BigDecimal("150.00"), savedLedger.getAmount());
        assertEquals(TransactionRecord.TransactionType.WITHDRAWAL, savedLedger.getType());
    }

    @Test
    void shouldThrowSecurityExceptionWhenWithdrawingAsWrongUser() {
        // Arrange
        String accountNumber = "1234567890";
        Account existingAccount = new Account("uuid-1", "REAL-OWNER", accountNumber, new BigDecimal("500.00"), 1L);
        WithdrawCommand command = new WithdrawCommand(accountNumber, new BigDecimal("150.00"), "HACKER");

        when(accountRepository.findByAccountNumberForWrite(accountNumber)).thenReturn(Optional.of(existingAccount));

        // Act & Assert
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            bankAccountService.withdraw(command);
        });

        assertEquals("You are not authorized to withdraw from this account", exception.getMessage());
        verify(accountRepository, never()).save(any());
        verify(transactionRecordRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenAccountNotFoundForWithdrawal() {
        // Arrange
        String accountNumber = "0000000000";
        WithdrawCommand command = new WithdrawCommand(accountNumber, new BigDecimal("100.00"), "USER-1");

        // Simulate database returning empty
        when(accountRepository.findByAccountNumberForWrite(accountNumber)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> {
            bankAccountService.withdraw(command);
        });

        assertEquals("Account not found", exception.getMessage());

        // Crucial: Ensure we NEVER attempt to save if the account wasn't found
        verify(accountRepository).findByAccountNumberForWrite(accountNumber);
        verify(accountRepository, never()).save(any());
        verify(transactionRecordRepository, never()).save(any());
    }

    @Test
    void shouldAbortTransactionWhenDomainThrowsInsufficientFunds() {
        // Arrange
        String requesterId = "USER-123";
        String accountNumber = "1234567890";
        Account existingAccount = new Account("uuid-1", requesterId, accountNumber, new BigDecimal("50.00"), 1L);
        WithdrawCommand command = new WithdrawCommand(accountNumber, new BigDecimal("100.00"), requesterId);

        when(accountRepository.findByAccountNumberForWrite(accountNumber)).thenReturn(Optional.of(existingAccount));

        // Act & Assert
        InsufficientFundsException exception = assertThrows(InsufficientFundsException.class, () -> {
            bankAccountService.withdraw(command);
        });

        assertEquals("Insufficient funds", exception.getMessage());

        // Crucial: Ensure the service respects the domain exception and ABORTS the save
        // operation
        verify(accountRepository).findByAccountNumberForWrite(accountNumber);
        verify(accountRepository, never()).save(any());
        verify(transactionRecordRepository, never()).save(any());
    }
}