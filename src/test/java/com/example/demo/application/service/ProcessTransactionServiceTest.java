package com.example.demo.application.service;

import com.example.demo.application.port.out.AccountRepository;
import com.example.demo.application.port.out.TransactionRecordRepository;
import com.example.demo.domain.event.TransactionPendingEvent;
import com.example.demo.domain.model.Account;
import com.example.demo.domain.model.TransactionRecord;
import com.example.demo.domain.model.TransactionRecord.TransactionStatus;
import com.example.demo.domain.model.TransactionRecord.TransactionType;
import com.example.demo.domain.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessTransactionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRecordRepository transactionRecordRepository;

    private ProcessTransactionService processTransactionService;

    @Captor
    private ArgumentCaptor<TransactionRecord> transactionCaptor;

    @BeforeEach
    void setUp() {
        processTransactionService = new ProcessTransactionService(accountRepository, transactionRecordRepository);
    }

    @Test
    void shouldSuccessfullyProcessPendingDepositEvent() {
        String txId = "tx-123";
        String accountNumber = "A1B2C3D4E5";
        TransactionRecord pendingTx = new TransactionRecord(
                txId, null, accountNumber, new BigDecimal("100.00"),
                TransactionType.DEPOSIT, LocalDateTime.now(), TransactionStatus.PENDING, null
        );
        TransactionPendingEvent event = new TransactionPendingEvent(
                UUID.randomUUID(), txId, Instant.now(), null, accountNumber,
                new BigDecimal("100.00"), TransactionType.DEPOSIT, "USER-123"
        );
        Account targetAccount = new Account("uuid-123", "USER-123", accountNumber, BigDecimal.ZERO, 1L);

        when(transactionRecordRepository.findById(txId)).thenReturn(Optional.of(pendingTx));
        when(accountRepository.lockAndLoad(accountNumber)).thenReturn(Optional.of(targetAccount));

        processTransactionService.process(event);

        assertEquals(new BigDecimal("100.00"), targetAccount.getBalance());
        verify(accountRepository, times(1)).save(targetAccount);

        verify(transactionRecordRepository, times(1)).save(transactionCaptor.capture());
        TransactionRecord finalTx = transactionCaptor.getValue();
        assertEquals(txId, finalTx.getId());
        assertEquals(TransactionStatus.COMPLETED, finalTx.getStatus());
        assertNull(finalTx.getFailureReason());
    }

    @Test
    void shouldSuccessfullyProcessPendingWithdrawEvent() {
        String txId = "tx-456";
        String accountNumber = "A1B2C3D4E5";
        TransactionRecord pendingTx = new TransactionRecord(
                txId, accountNumber, null, new BigDecimal("50.00"),
                TransactionType.WITHDRAWAL, LocalDateTime.now(), TransactionStatus.PENDING, null
        );
        TransactionPendingEvent event = new TransactionPendingEvent(
                UUID.randomUUID(), txId, Instant.now(), accountNumber, null,
                new BigDecimal("50.00"), TransactionType.WITHDRAWAL, "USER-123"
        );
        Account sourceAccount = new Account("uuid-123", "USER-123", accountNumber, new BigDecimal("100.00"), 1L);

        when(transactionRecordRepository.findById(txId)).thenReturn(Optional.of(pendingTx));
        when(accountRepository.lockAndLoad(accountNumber)).thenReturn(Optional.of(sourceAccount));

        processTransactionService.process(event);

        assertEquals(new BigDecimal("50.00"), sourceAccount.getBalance());
        verify(accountRepository, times(1)).save(sourceAccount);

        verify(transactionRecordRepository, times(1)).save(transactionCaptor.capture());
        TransactionRecord finalTx = transactionCaptor.getValue();
        assertEquals(txId, finalTx.getId());
        assertEquals(TransactionStatus.COMPLETED, finalTx.getStatus());
    }

    @Test
    void shouldMarkTransactionAsFailedWhenInsufficientFunds() {
        String txId = "tx-789";
        String accountNumber = "A1B2C3D4E5";
        TransactionRecord pendingTx = new TransactionRecord(
                txId, accountNumber, null, new BigDecimal("150.00"),
                TransactionType.WITHDRAWAL, LocalDateTime.now(), TransactionStatus.PENDING, null
        );
        TransactionPendingEvent event = new TransactionPendingEvent(
                UUID.randomUUID(), txId, Instant.now(), accountNumber, null,
                new BigDecimal("150.00"), TransactionType.WITHDRAWAL, "USER-123"
        );
        Account sourceAccount = new Account("uuid-123", "USER-123", accountNumber, new BigDecimal("100.00"), 1L);

        when(transactionRecordRepository.findById(txId)).thenReturn(Optional.of(pendingTx));
        when(accountRepository.lockAndLoad(accountNumber)).thenReturn(Optional.of(sourceAccount));

        processTransactionService.process(event);

        verify(accountRepository, never()).save(sourceAccount);

        verify(transactionRecordRepository, times(1)).save(transactionCaptor.capture());
        TransactionRecord finalTx = transactionCaptor.getValue();
        assertEquals(txId, finalTx.getId());
        assertEquals(TransactionStatus.FAILED, finalTx.getStatus());
        assertEquals("Insufficient funds", finalTx.getFailureReason());
    }

    @Test
    void shouldThrowEntityNotFoundExceptionWhenTransactionRecordNotFound() {
        String txId = "NON-EXISTENT";
        TransactionPendingEvent event = new TransactionPendingEvent(
                UUID.randomUUID(), txId, Instant.now(), null, "ACC123",
                new BigDecimal("100.00"), TransactionType.DEPOSIT, "USER-123"
        );

        when(transactionRecordRepository.findById(txId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> processTransactionService.process(event));
        verify(accountRepository, never()).lockAndLoad(anyString());
    }

    @Test
    void shouldMarkTransactionAsFailedWhenTargetAccountNotFoundForDeposit() {
        String txId = "tx-123";
        String accountNumber = "A1B2C3D4E5";
        TransactionRecord pendingTx = new TransactionRecord(
                txId, null, accountNumber, new BigDecimal("100.00"),
                TransactionType.DEPOSIT, LocalDateTime.now(), TransactionStatus.PENDING, null
        );
        TransactionPendingEvent event = new TransactionPendingEvent(
                UUID.randomUUID(), txId, Instant.now(), null, accountNumber,
                new BigDecimal("100.00"), TransactionType.DEPOSIT, "USER-123"
        );

        when(transactionRecordRepository.findById(txId)).thenReturn(Optional.of(pendingTx));
        when(accountRepository.lockAndLoad(accountNumber)).thenReturn(Optional.empty());

        processTransactionService.process(event);

        verify(transactionRecordRepository, times(1)).save(transactionCaptor.capture());
        TransactionRecord finalTx = transactionCaptor.getValue();
        assertEquals(txId, finalTx.getId());
        assertEquals(TransactionStatus.FAILED, finalTx.getStatus());
        assertEquals("Target account not found", finalTx.getFailureReason());
    }

    @Test
    void shouldMarkTransactionAsFailedWhenSourceAccountNotFoundForWithdrawal() {
        String txId = "tx-456";
        String accountNumber = "A1B2C3D4E5";
        TransactionRecord pendingTx = new TransactionRecord(
                txId, accountNumber, null, new BigDecimal("50.00"),
                TransactionType.WITHDRAWAL, LocalDateTime.now(), TransactionStatus.PENDING, null
        );
        TransactionPendingEvent event = new TransactionPendingEvent(
                UUID.randomUUID(), txId, Instant.now(), accountNumber, null,
                new BigDecimal("50.00"), TransactionType.WITHDRAWAL, "USER-123"
        );

        when(transactionRecordRepository.findById(txId)).thenReturn(Optional.of(pendingTx));
        when(accountRepository.lockAndLoad(accountNumber)).thenReturn(Optional.empty());

        processTransactionService.process(event);

        verify(transactionRecordRepository, times(1)).save(transactionCaptor.capture());
        TransactionRecord finalTx = transactionCaptor.getValue();
        assertEquals(txId, finalTx.getId());
        assertEquals(TransactionStatus.FAILED, finalTx.getStatus());
        assertEquals("Source account not found", finalTx.getFailureReason());
    }

    @Test
    void shouldIgnoreProcessingWhenTransactionAlreadyProcessed() {
        String txId = "tx-789";
        TransactionRecord completedTx = new TransactionRecord(
                txId, null, "ACC123", new BigDecimal("100.00"),
                TransactionType.DEPOSIT, LocalDateTime.now(), TransactionStatus.COMPLETED, null
        );
        TransactionPendingEvent event = new TransactionPendingEvent(
                UUID.randomUUID(), txId, Instant.now(), null, "ACC123",
                new BigDecimal("100.00"), TransactionType.DEPOSIT, "USER-123"
        );

        when(transactionRecordRepository.findById(txId)).thenReturn(Optional.of(completedTx));

        processTransactionService.process(event);

        verify(accountRepository, never()).lockAndLoad(anyString());
        verify(transactionRecordRepository, never()).save(any());
    }
}
