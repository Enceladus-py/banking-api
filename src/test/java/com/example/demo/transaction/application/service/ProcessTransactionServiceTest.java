package com.example.demo.transaction.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.account.application.port.in.AccountOperationsPort;
import com.example.demo.account.domain.model.Account;
import com.example.demo.common.domain.exception.EntityNotFoundException;
import com.example.demo.transaction.application.port.out.TransactionRecordRepository;
import com.example.demo.transaction.domain.event.TransactionPendingEvent;
import com.example.demo.transaction.domain.model.TransactionRecord;
import com.example.demo.transaction.domain.model.TransactionRecord.TransactionStatus;
import com.example.demo.transaction.domain.model.TransactionRecord.TransactionType;

@ExtendWith(MockitoExtension.class)
class ProcessTransactionServiceTest {

	@Mock
	private AccountOperationsPort accountOperationsPort;

	@Mock
	private TransactionRecordRepository transactionRecordRepository;

	private ProcessTransactionService processTransactionService;

	@Captor
	private ArgumentCaptor<TransactionRecord> transactionCaptor;

	@BeforeEach
	void setUp() {
		processTransactionService = new ProcessTransactionService(accountOperationsPort, transactionRecordRepository);
	}

	@Test
	void shouldSuccessfullyProcessPendingDepositEvent() {
		String txId = "tx-123";
		String accountNumber = "A1B2C3D4E5";
		TransactionRecord pendingTx = new TransactionRecord(txId, null, accountNumber, new BigDecimal("100.00"),
				TransactionType.DEPOSIT, LocalDateTime.now(), TransactionStatus.PENDING, null);
		TransactionPendingEvent event = new TransactionPendingEvent(UUID.randomUUID(), txId, Instant.now(), null,
				accountNumber, new BigDecimal("100.00"), TransactionType.DEPOSIT, "USER-123");
		Account targetAccount = new Account("uuid-123", "USER-123", accountNumber, BigDecimal.ZERO, 1L);

		when(transactionRecordRepository.findById(txId)).thenReturn(Optional.of(pendingTx));
		when(accountOperationsPort.lockAndLoad(accountNumber)).thenReturn(Optional.of(targetAccount));

		processTransactionService.process(event);

		assertEquals(new BigDecimal("100.00"), targetAccount.getBalance());
		verify(accountOperationsPort, times(1)).save(targetAccount);

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
		TransactionRecord pendingTx = new TransactionRecord(txId, accountNumber, null, new BigDecimal("50.00"),
				TransactionType.WITHDRAWAL, LocalDateTime.now(), TransactionStatus.PENDING, null);
		TransactionPendingEvent event = new TransactionPendingEvent(UUID.randomUUID(), txId, Instant.now(),
				accountNumber, null, new BigDecimal("50.00"), TransactionType.WITHDRAWAL, "USER-123");
		Account sourceAccount = new Account("uuid-123", "USER-123", accountNumber, new BigDecimal("100.00"), 1L);

		when(transactionRecordRepository.findById(txId)).thenReturn(Optional.of(pendingTx));
		when(accountOperationsPort.lockAndLoad(accountNumber)).thenReturn(Optional.of(sourceAccount));

		processTransactionService.process(event);

		assertEquals(new BigDecimal("50.00"), sourceAccount.getBalance());
		verify(accountOperationsPort, times(1)).save(sourceAccount);

		verify(transactionRecordRepository, times(1)).save(transactionCaptor.capture());
		TransactionRecord finalTx = transactionCaptor.getValue();
		assertEquals(txId, finalTx.getId());
		assertEquals(TransactionStatus.COMPLETED, finalTx.getStatus());
	}

	@Test
	void shouldMarkTransactionAsFailedWhenInsufficientFunds() {
		String txId = "tx-789";
		String accountNumber = "A1B2C3D4E5";
		TransactionRecord pendingTx = new TransactionRecord(txId, accountNumber, null, new BigDecimal("150.00"),
				TransactionType.WITHDRAWAL, LocalDateTime.now(), TransactionStatus.PENDING, null);
		TransactionPendingEvent event = new TransactionPendingEvent(UUID.randomUUID(), txId, Instant.now(),
				accountNumber, null, new BigDecimal("150.00"), TransactionType.WITHDRAWAL, "USER-123");
		Account sourceAccount = new Account("uuid-123", "USER-123", accountNumber, new BigDecimal("100.00"), 1L);

		when(transactionRecordRepository.findById(txId)).thenReturn(Optional.of(pendingTx));
		when(accountOperationsPort.lockAndLoad(accountNumber)).thenReturn(Optional.of(sourceAccount));

		processTransactionService.process(event);

		verify(accountOperationsPort, never()).save(sourceAccount);

		verify(transactionRecordRepository, times(1)).save(transactionCaptor.capture());
		TransactionRecord finalTx = transactionCaptor.getValue();
		assertEquals(txId, finalTx.getId());
		assertEquals(TransactionStatus.FAILED, finalTx.getStatus());
		assertEquals("Insufficient funds", finalTx.getFailureReason());
	}

	@Test
	void shouldThrowEntityNotFoundExceptionWhenTransactionRecordNotFound() {
		String txId = "NON-EXISTENT";
		TransactionPendingEvent event = new TransactionPendingEvent(UUID.randomUUID(), txId, Instant.now(), null,
				"ACC123", new BigDecimal("100.00"), TransactionType.DEPOSIT, "USER-123");

		when(transactionRecordRepository.findById(txId)).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> processTransactionService.process(event));
		verify(accountOperationsPort, never()).lockAndLoad(anyString());
	}

	@Test
	void shouldMarkTransactionAsFailedWhenTargetAccountNotFoundForDeposit() {
		String txId = "tx-123";
		String accountNumber = "A1B2C3D4E5";
		TransactionRecord pendingTx = new TransactionRecord(txId, null, accountNumber, new BigDecimal("100.00"),
				TransactionType.DEPOSIT, LocalDateTime.now(), TransactionStatus.PENDING, null);
		TransactionPendingEvent event = new TransactionPendingEvent(UUID.randomUUID(), txId, Instant.now(), null,
				accountNumber, new BigDecimal("100.00"), TransactionType.DEPOSIT, "USER-123");

		when(transactionRecordRepository.findById(txId)).thenReturn(Optional.of(pendingTx));
		when(accountOperationsPort.lockAndLoad(accountNumber)).thenReturn(Optional.empty());

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
		TransactionRecord pendingTx = new TransactionRecord(txId, accountNumber, null, new BigDecimal("50.00"),
				TransactionType.WITHDRAWAL, LocalDateTime.now(), TransactionStatus.PENDING, null);
		TransactionPendingEvent event = new TransactionPendingEvent(UUID.randomUUID(), txId, Instant.now(),
				accountNumber, null, new BigDecimal("50.00"), TransactionType.WITHDRAWAL, "USER-123");

		when(transactionRecordRepository.findById(txId)).thenReturn(Optional.of(pendingTx));
		when(accountOperationsPort.lockAndLoad(accountNumber)).thenReturn(Optional.empty());

		processTransactionService.process(event);

		verify(transactionRecordRepository, times(1)).save(transactionCaptor.capture());
		TransactionRecord finalTx = transactionCaptor.getValue();
		assertEquals(txId, finalTx.getId());
		assertEquals(TransactionStatus.FAILED, finalTx.getStatus());
		assertEquals("Source account not found", finalTx.getFailureReason());
	}

	@Test
	void shouldSuccessfullyProcessPendingTransferEvent() {
		String txId = "tx-transfer";
		String sourceAccountNumber = "A1B2C3D4E5"; // First alphabetically
		String targetAccountNumber = "X9Y8Z7W6V5"; // Second alphabetically
		TransactionRecord pendingTx = new TransactionRecord(txId, sourceAccountNumber, targetAccountNumber,
				new BigDecimal("50.00"), TransactionType.TRANSFER, LocalDateTime.now(), TransactionStatus.PENDING,
				null);
		TransactionPendingEvent event = new TransactionPendingEvent(UUID.randomUUID(), txId, Instant.now(),
				sourceAccountNumber, targetAccountNumber, new BigDecimal("50.00"), TransactionType.TRANSFER,
				"USER-123");
		Account sourceAccount = new Account("uuid-1", "USER-123", sourceAccountNumber, new BigDecimal("100.00"), 1L);
		Account targetAccount = new Account("uuid-2", "USER-456", targetAccountNumber, new BigDecimal("20.00"), 1L);

		when(transactionRecordRepository.findById(txId)).thenReturn(Optional.of(pendingTx));
		when(accountOperationsPort.lockAndLoad(sourceAccountNumber)).thenReturn(Optional.of(sourceAccount));
		when(accountOperationsPort.lockAndLoad(targetAccountNumber)).thenReturn(Optional.of(targetAccount));

		processTransactionService.process(event);

		assertEquals(new BigDecimal("50.00"), sourceAccount.getBalance());
		assertEquals(new BigDecimal("70.00"), targetAccount.getBalance());
		verify(accountOperationsPort, times(1)).save(sourceAccount);
		verify(accountOperationsPort, times(1)).save(targetAccount);

		verify(transactionRecordRepository, times(1)).save(transactionCaptor.capture());
		TransactionRecord finalTx = transactionCaptor.getValue();
		assertEquals(txId, finalTx.getId());
		assertEquals(TransactionStatus.COMPLETED, finalTx.getStatus());
	}

	@Test
	void shouldSuccessfullyProcessPendingTransferEventReverseAlphabetical() {
		String txId = "tx-transfer-rev";
		String sourceAccountNumber = "X9Y8Z7W6V5"; // Second alphabetically
		String targetAccountNumber = "A1B2C3D4E5"; // First alphabetically
		TransactionRecord pendingTx = new TransactionRecord(txId, sourceAccountNumber, targetAccountNumber,
				new BigDecimal("50.00"), TransactionType.TRANSFER, LocalDateTime.now(), TransactionStatus.PENDING,
				null);
		TransactionPendingEvent event = new TransactionPendingEvent(UUID.randomUUID(), txId, Instant.now(),
				sourceAccountNumber, targetAccountNumber, new BigDecimal("50.00"), TransactionType.TRANSFER,
				"USER-123");
		Account sourceAccount = new Account("uuid-1", "USER-123", sourceAccountNumber, new BigDecimal("100.00"), 1L);
		Account targetAccount = new Account("uuid-2", "USER-456", targetAccountNumber, new BigDecimal("20.00"), 1L);

		when(transactionRecordRepository.findById(txId)).thenReturn(Optional.of(pendingTx));
		when(accountOperationsPort.lockAndLoad(targetAccountNumber)).thenReturn(Optional.of(targetAccount));
		when(accountOperationsPort.lockAndLoad(sourceAccountNumber)).thenReturn(Optional.of(sourceAccount));

		processTransactionService.process(event);

		assertEquals(new BigDecimal("50.00"), sourceAccount.getBalance());
		assertEquals(new BigDecimal("70.00"), targetAccount.getBalance());
		verify(accountOperationsPort, times(1)).save(sourceAccount);
		verify(accountOperationsPort, times(1)).save(targetAccount);

		verify(transactionRecordRepository, times(1)).save(transactionCaptor.capture());
		TransactionRecord finalTx = transactionCaptor.getValue();
		assertEquals(txId, finalTx.getId());
		assertEquals(TransactionStatus.COMPLETED, finalTx.getStatus());
	}

	@Test
	void shouldMarkTransactionAsFailedWhenFirstAccountNotFoundForTransfer() {
		String txId = "tx-transfer";
		String sourceAccountNumber = "A1B2C3D4E5";
		String targetAccountNumber = "X9Y8Z7W6V5";
		TransactionRecord pendingTx = new TransactionRecord(txId, sourceAccountNumber, targetAccountNumber,
				new BigDecimal("50.00"), TransactionType.TRANSFER, LocalDateTime.now(), TransactionStatus.PENDING,
				null);
		TransactionPendingEvent event = new TransactionPendingEvent(UUID.randomUUID(), txId, Instant.now(),
				sourceAccountNumber, targetAccountNumber, new BigDecimal("50.00"), TransactionType.TRANSFER,
				"USER-123");

		when(transactionRecordRepository.findById(txId)).thenReturn(Optional.of(pendingTx));
		when(accountOperationsPort.lockAndLoad(sourceAccountNumber)).thenReturn(Optional.empty()); // first
																									// alphabetically

		processTransactionService.process(event);

		verify(transactionRecordRepository, times(1)).save(transactionCaptor.capture());
		TransactionRecord finalTx = transactionCaptor.getValue();
		assertEquals(txId, finalTx.getId());
		assertEquals(TransactionStatus.FAILED, finalTx.getStatus());
		assertEquals("Account not found: " + sourceAccountNumber, finalTx.getFailureReason());
	}

	@Test
	void shouldMarkTransactionAsFailedWhenSecondAccountNotFoundForTransfer() {
		String txId = "tx-transfer";
		String sourceAccountNumber = "A1B2C3D4E5";
		String targetAccountNumber = "X9Y8Z7W6V5";
		TransactionRecord pendingTx = new TransactionRecord(txId, sourceAccountNumber, targetAccountNumber,
				new BigDecimal("50.00"), TransactionType.TRANSFER, LocalDateTime.now(), TransactionStatus.PENDING,
				null);
		TransactionPendingEvent event = new TransactionPendingEvent(UUID.randomUUID(), txId, Instant.now(),
				sourceAccountNumber, targetAccountNumber, new BigDecimal("50.00"), TransactionType.TRANSFER,
				"USER-123");
		Account sourceAccount = new Account("uuid-1", "USER-123", sourceAccountNumber, new BigDecimal("100.00"), 1L);

		when(transactionRecordRepository.findById(txId)).thenReturn(Optional.of(pendingTx));
		when(accountOperationsPort.lockAndLoad(sourceAccountNumber)).thenReturn(Optional.of(sourceAccount)); // first
																												// alphabetically
		when(accountOperationsPort.lockAndLoad(targetAccountNumber)).thenReturn(Optional.empty()); // second
																									// alphabetically

		processTransactionService.process(event);

		verify(transactionRecordRepository, times(1)).save(transactionCaptor.capture());
		TransactionRecord finalTx = transactionCaptor.getValue();
		assertEquals(txId, finalTx.getId());
		assertEquals(TransactionStatus.FAILED, finalTx.getStatus());
		assertEquals("Account not found: " + targetAccountNumber, finalTx.getFailureReason());
	}

	@Test
	void shouldIgnoreProcessingWhenTransactionAlreadyProcessed() {
		String txId = "tx-789";
		TransactionRecord completedTx = new TransactionRecord(txId, null, "ACC123", new BigDecimal("100.00"),
				TransactionType.DEPOSIT, LocalDateTime.now(), TransactionStatus.COMPLETED, null);
		TransactionPendingEvent event = new TransactionPendingEvent(UUID.randomUUID(), txId, Instant.now(), null,
				"ACC123", new BigDecimal("100.00"), TransactionType.DEPOSIT, "USER-123");

		when(transactionRecordRepository.findById(txId)).thenReturn(Optional.of(completedTx));

		processTransactionService.process(event);

		verify(accountOperationsPort, never()).lockAndLoad(anyString());
		verify(transactionRecordRepository, never()).save(any());
	}
}
