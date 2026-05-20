package com.example.demo.application.service;

import com.example.demo.application.port.in.CreateAccountUseCase.CreateAccountCommand;
import com.example.demo.application.port.out.AccountRepository;
import com.example.demo.domain.model.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
}