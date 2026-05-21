package com.example.demo.application.service;

import com.example.demo.application.port.in.TransferMoneyUseCase.TransferCommand;
import com.example.demo.application.port.out.AccountRepository;
import com.example.demo.application.port.out.TransactionRecordRepository;
import com.example.demo.domain.model.Account;
import com.example.demo.domain.model.TransactionRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRecordRepository transactionRecordRepository;

    @InjectMocks
    private TransferService transferService;

    @Captor
    private ArgumentCaptor<TransactionRecord> transactionCaptor;

    @Test
    void shouldTransferMoneySuccessfullyAndSaveLedger() {
        // Arrange
        String sourceId = "SRC1234567";
        String targetId = "TGT1234567";
        BigDecimal amount = new BigDecimal("150.00");

        Account sourceAccount = new Account("uuid-1", "Berat", "Dalsuna", sourceId, new BigDecimal("500.00"));
        Account targetAccount = new Account("uuid-2", "John", "Doe", targetId, new BigDecimal("100.00"));

        TransferCommand command = new TransferCommand(sourceId, targetId, amount);

        when(accountRepository.findByAccountNumber(sourceId)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumber(targetId)).thenReturn(Optional.of(targetAccount));

        // Act
        transferService.transfer(command);

        // Assert Account States
        assertEquals(new BigDecimal("350.00"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("250.00"), targetAccount.getBalance());

        // Verify saves
        verify(accountRepository).save(sourceAccount);
        verify(accountRepository).save(targetAccount);

        // Verify Ledger
        verify(transactionRecordRepository).save(transactionCaptor.capture());
        TransactionRecord savedLedger = transactionCaptor.getValue();
        assertEquals(sourceId, savedLedger.getSourceAccountNumber());
        assertEquals(targetId, savedLedger.getTargetAccountNumber());
        assertEquals(amount, savedLedger.getAmount());
        assertEquals(TransactionRecord.TransactionType.TRANSFER, savedLedger.getType());
    }

    @Test
    void shouldAbortWhenSourceAccountHasInsufficientFunds() {
        // Arrange
        String sourceId = "SRC1234567";
        String targetId = "TGT1234567";
        Account sourceAccount = new Account("uuid-1", "Berat", "Dalsuna", sourceId, new BigDecimal("50.00"));
        Account targetAccount = new Account("uuid-2", "John", "Doe", targetId, new BigDecimal("100.00"));

        TransferCommand command = new TransferCommand(sourceId, targetId, new BigDecimal("1000.00"));

        when(accountRepository.findByAccountNumber(sourceId)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumber(targetId)).thenReturn(Optional.of(targetAccount));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            transferService.transfer(command);
        });

        assertEquals("Insufficient funds", exception.getMessage());

        // Verify completely aborted
        verify(accountRepository, never()).save(any());
        verify(transactionRecordRepository, never()).save(any());
    }

    @Test
    void shouldAbortWhenTargetAccountNotFound() {
        // Arrange
        String sourceId = "SRC1234567";
        String targetId = "TGT1234567";
        Account sourceAccount = new Account("uuid-1", "Berat", "Dalsuna", sourceId, new BigDecimal("500.00"));

        TransferCommand command = new TransferCommand(sourceId, targetId, new BigDecimal("100.00"));

        when(accountRepository.findByAccountNumber(sourceId)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumber(targetId)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            transferService.transfer(command);
        });

        assertEquals("Target account not found", exception.getMessage());

        verify(accountRepository, never()).save(any());
        verify(transactionRecordRepository, never()).save(any());
    }
}