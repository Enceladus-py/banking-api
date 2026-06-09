package com.example.demo.transaction.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Instant;
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
import com.example.demo.transaction.application.port.out.EventPublisher;
import com.example.demo.transaction.application.port.out.ProcessedEventPort;
import com.example.demo.transaction.application.port.out.TransactionRecordRepository;
import com.example.demo.transaction.domain.event.TransactionCompletedEvent;
import com.example.demo.transaction.domain.event.TransactionEvent;
import com.example.demo.transaction.domain.event.TransactionFailedEvent;
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

	@Mock
	private ProcessedEventPort processedEventPort;

	@Mock
	private EventPublisher eventPublisher;

	private ProcessTransactionService processTransactionService;

	@Captor
	private ArgumentCaptor<TransactionRecord> transactionCaptor;

	@Captor
	private ArgumentCaptor<TransactionEvent> eventCaptor;

	@BeforeEach
	void setUp() {
		processTransactionService = new ProcessTransactionService(accountOperationsPort, transactionRecordRepository,
				processedEventPort, eventPublisher);
	}

	@Test
	void shouldSuccessfullyProcessPendingDepositEvent() {
		String txId = "tx-123";
		String accountNumber = "A1B2C3D4E5";
		TransactionRecord pendingTx = TransactionRecord.reconstitute(txId, null, accountNumber,
				new BigDecimal("100.00"), TransactionType.DEPOSIT, Instant.now(), TransactionStatus.PENDING, null);
		TransactionPendingEvent event = new TransactionPendingEvent(UUID.randomUUID(), txId, Instant.now(), null,
				accountNumber, new BigDecimal("100.00"), TransactionType.DEPOSIT, "USER-123");
		Account targetAccount = Account.reconstitute("uuid-123", "USER-123", accountNumber, BigDecimal.ZERO);

		when(transactionRecordRepository.findById(txId)).thenReturn(Optional.of(pendingTx));
		when(accountOperationsPort.lockAndLoad(accountNumber)).thenReturn(Optional.of(targetAccount));
		when(processedEventPort.saveIfAbsent(any())).thenReturn(true);

		processTransactionService.process(event);

		assertEquals(new BigDecimal("100.00"), targetAccount.getBalance());
		verify(accountOperationsPort, times(1)).save(targetAccount);

		verify(transactionRecordRepository, times(1)).save(transactionCaptor.capture());
		TransactionRecord finalTx = transactionCaptor.getValue();
		assertEquals(txId, finalTx.getId());
		assertEquals(TransactionStatus.COMPLETED, finalTx.getStatus());
		assertNull(finalTx.getFailureReason());

		verify(eventPublisher, times(1)).publish(eventCaptor.capture());
		TransactionCompletedEvent completedEvent = (TransactionCompletedEvent) eventCaptor.getValue();
		assertEquals(txId, completedEvent.transactionId());
	}

	@Test
	void shouldSuccessfullyProcessPendingWithdrawEvent() {
		String txId = "tx-456";
		String accountNumber = "A1B2C3D4E5";
		TransactionRecord pendingTx = TransactionRecord.reconstitute(txId, accountNumber, null, new BigDecimal("50.00"),
				TransactionType.WITHDRAWAL, Instant.now(), TransactionStatus.PENDING, null);
		TransactionPendingEvent event = new TransactionPendingEvent(UUID.randomUUID(), txId, Instant.now(),
				accountNumber, null, new BigDecimal("50.00"), TransactionType.WITHDRAWAL, "USER-123");
		Account sourceAccount = Account.reconstitute("uuid-123", "USER-123", accountNumber, new BigDecimal("100.00"));

		when(transactionRecordRepository.findById(txId)).thenReturn(Optional.of(pendingTx));
		when(accountOperationsPort.lockAndLoad(accountNumber)).thenReturn(Optional.of(sourceAccount));
		when(processedEventPort.saveIfAbsent(any())).thenReturn(true);

		processTransactionService.process(event);

		assertEquals(new BigDecimal("50.00"), sourceAccount.getBalance());
		verify(accountOperationsPort, times(1)).save(sourceAccount);

		verify(transactionRecordRepository, times(1)).save(transactionCaptor.capture());
		TransactionRecord finalTx = transactionCaptor.getValue();
		assertEquals(txId, finalTx.getId());
		assertEquals(TransactionStatus.COMPLETED, finalTx.getStatus());

		verify(eventPublisher, times(1)).publish(eventCaptor.capture());
		TransactionCompletedEvent completedEvent = (TransactionCompletedEvent) eventCaptor.getValue();
		assertEquals(txId, completedEvent.transactionId());
	}

	@Test
	void shouldMarkTransactionAsFailedWhenInsufficientFunds() {
		String txId = "tx-789";
		String accountNumber = "A1B2C3D4E5";
		TransactionRecord pendingTx = TransactionRecord.reconstitute(txId, accountNumber, null,
				new BigDecimal("150.00"), TransactionType.WITHDRAWAL, Instant.now(), TransactionStatus.PENDING, null);
		TransactionPendingEvent event = new TransactionPendingEvent(UUID.randomUUID(), txId, Instant.now(),
				accountNumber, null, new BigDecimal("150.00"), TransactionType.WITHDRAWAL, "USER-123");
		Account sourceAccount = Account.reconstitute("uuid-123", "USER-123", accountNumber, new BigDecimal("100.00"));

		when(transactionRecordRepository.findById(txId)).thenReturn(Optional.of(pendingTx));
		when(accountOperationsPort.lockAndLoad(accountNumber)).thenReturn(Optional.of(sourceAccount));
		when(processedEventPort.saveIfAbsent(any())).thenReturn(true);

		processTransactionService.process(event);

		verify(accountOperationsPort, never()).save(sourceAccount);

		verify(transactionRecordRepository, times(1)).save(transactionCaptor.capture());
		TransactionRecord finalTx = transactionCaptor.getValue();
		assertEquals(txId, finalTx.getId());
		assertEquals(TransactionStatus.FAILED, finalTx.getStatus());
		assertEquals("Insufficient funds", finalTx.getFailureReason());

		verify(eventPublisher, times(1)).publish(eventCaptor.capture());
		TransactionFailedEvent failedEvent = (TransactionFailedEvent) eventCaptor.getValue();
		assertEquals(txId, failedEvent.transactionId());
		assertEquals("Insufficient funds", failedEvent.failureReason());
	}

	@Test
	void shouldThrowEntityNotFoundExceptionWhenTransactionRecordNotFound() {
		String txId = "NON-EXISTENT";
		TransactionPendingEvent event = new TransactionPendingEvent(UUID.randomUUID(), txId, Instant.now(), null,
				"ACC123", new BigDecimal("100.00"), TransactionType.DEPOSIT, "USER-123");

		when(transactionRecordRepository.findById(txId)).thenReturn(Optional.empty());
		when(processedEventPort.saveIfAbsent(any())).thenReturn(true);

		assertThrows(EntityNotFoundException.class, () -> processTransactionService.process(event));
		verify(accountOperationsPort, never()).lockAndLoad(anyString());
		verify(eventPublisher, never()).publish(any());
	}

	@Test
	void shouldMarkTransactionAsFailedWhenTargetAccountNotFoundForDeposit() {
		String txId = "tx-123";
		String accountNumber = "A1B2C3D4E5";
		TransactionRecord pendingTx = TransactionRecord.reconstitute(txId, null, accountNumber,
				new BigDecimal("100.00"), TransactionType.DEPOSIT, Instant.now(), TransactionStatus.PENDING, null);
		TransactionPendingEvent event = new TransactionPendingEvent(UUID.randomUUID(), txId, Instant.now(), null,
				accountNumber, new BigDecimal("100.00"), TransactionType.DEPOSIT, "USER-123");

		when(transactionRecordRepository.findById(txId)).thenReturn(Optional.of(pendingTx));
		when(accountOperationsPort.lockAndLoad(accountNumber)).thenReturn(Optional.empty());
		when(processedEventPort.saveIfAbsent(any())).thenReturn(true);

		processTransactionService.process(event);

		verify(transactionRecordRepository, times(1)).save(transactionCaptor.capture());
		TransactionRecord finalTx = transactionCaptor.getValue();
		assertEquals(txId, finalTx.getId());
		assertEquals(TransactionStatus.FAILED, finalTx.getStatus());
		assertEquals("Target account not found", finalTx.getFailureReason());

		verify(eventPublisher, times(1)).publish(eventCaptor.capture());
		TransactionFailedEvent failedEvent = (TransactionFailedEvent) eventCaptor.getValue();
		assertEquals(txId, failedEvent.transactionId());
		assertEquals("Target account not found", failedEvent.failureReason());
	}

	@Test
	void shouldMarkTransactionAsFailedWhenSourceAccountNotFoundForWithdrawal() {
		String txId = "tx-456";
		String accountNumber = "A1B2C3D4E5";
		TransactionRecord pendingTx = TransactionRecord.reconstitute(txId, accountNumber, null, new BigDecimal("50.00"),
				TransactionType.WITHDRAWAL, Instant.now(), TransactionStatus.PENDING, null);
		TransactionPendingEvent event = new TransactionPendingEvent(UUID.randomUUID(), txId, Instant.now(),
				accountNumber, null, new BigDecimal("50.00"), TransactionType.WITHDRAWAL, "USER-123");

		when(transactionRecordRepository.findById(txId)).thenReturn(Optional.of(pendingTx));
		when(accountOperationsPort.lockAndLoad(accountNumber)).thenReturn(Optional.empty());
		when(processedEventPort.saveIfAbsent(any())).thenReturn(true);

		processTransactionService.process(event);

		verify(transactionRecordRepository, times(1)).save(transactionCaptor.capture());
		TransactionRecord finalTx = transactionCaptor.getValue();
		assertEquals(txId, finalTx.getId());
		assertEquals(TransactionStatus.FAILED, finalTx.getStatus());
		assertEquals("Source account not found", finalTx.getFailureReason());

		verify(eventPublisher, times(1)).publish(eventCaptor.capture());
		TransactionFailedEvent failedEvent = (TransactionFailedEvent) eventCaptor.getValue();
		assertEquals(txId, failedEvent.transactionId());
		assertEquals("Source account not found", failedEvent.failureReason());
	}

	@Test
	void shouldSuccessfullyProcessPendingTransferEvent() {
		String txId = "tx-transfer";
		String sourceAccountNumber = "A1B2C3D4E5"; // First alphabetically
		String targetAccountNumber = "X9Y8Z7W6V5"; // Second alphabetically
		TransactionRecord pendingTx = TransactionRecord.reconstitute(txId, sourceAccountNumber, targetAccountNumber,
				new BigDecimal("50.00"), TransactionType.TRANSFER, Instant.now(), TransactionStatus.PENDING, null);
		TransactionPendingEvent event = new TransactionPendingEvent(UUID.randomUUID(), txId, Instant.now(),
				sourceAccountNumber, targetAccountNumber, new BigDecimal("50.00"), TransactionType.TRANSFER,
				"USER-123");
		Account sourceAccount = Account.reconstitute("uuid-1", "USER-123", sourceAccountNumber,
				new BigDecimal("100.00"));
		Account targetAccount = Account.reconstitute("uuid-2", "USER-456", targetAccountNumber,
				new BigDecimal("20.00"));

		when(transactionRecordRepository.findById(txId)).thenReturn(Optional.of(pendingTx));
		when(accountOperationsPort.lockAndLoad(sourceAccountNumber)).thenReturn(Optional.of(sourceAccount));
		when(accountOperationsPort.lockAndLoad(targetAccountNumber)).thenReturn(Optional.of(targetAccount));
		when(processedEventPort.saveIfAbsent(any())).thenReturn(true);

		processTransactionService.process(event);

		assertEquals(new BigDecimal("50.00"), sourceAccount.getBalance());
		assertEquals(new BigDecimal("70.00"), targetAccount.getBalance());
		verify(accountOperationsPort, times(1)).save(sourceAccount);
		verify(accountOperationsPort, times(1)).save(targetAccount);

		verify(transactionRecordRepository, times(1)).save(transactionCaptor.capture());
		TransactionRecord finalTx = transactionCaptor.getValue();
		assertEquals(txId, finalTx.getId());
		assertEquals(TransactionStatus.COMPLETED, finalTx.getStatus());

		verify(eventPublisher, times(1)).publish(eventCaptor.capture());
		TransactionCompletedEvent completedEvent = (TransactionCompletedEvent) eventCaptor.getValue();
		assertEquals(txId, completedEvent.transactionId());
	}

	@Test
	void shouldSuccessfullyProcessPendingTransferEventReverseAlphabetical() {
		String txId = "tx-transfer-rev";
		String sourceAccountNumber = "X9Y8Z7W6V5"; // Second alphabetically
		String targetAccountNumber = "A1B2C3D4E5"; // First alphabetically
		TransactionRecord pendingTx = TransactionRecord.reconstitute(txId, sourceAccountNumber, targetAccountNumber,
				new BigDecimal("50.00"), TransactionType.TRANSFER, Instant.now(), TransactionStatus.PENDING, null);
		TransactionPendingEvent event = new TransactionPendingEvent(UUID.randomUUID(), txId, Instant.now(),
				sourceAccountNumber, targetAccountNumber, new BigDecimal("50.00"), TransactionType.TRANSFER,
				"USER-123");
		Account sourceAccount = Account.reconstitute("uuid-1", "USER-123", sourceAccountNumber,
				new BigDecimal("100.00"));
		Account targetAccount = Account.reconstitute("uuid-2", "USER-456", targetAccountNumber,
				new BigDecimal("20.00"));

		when(transactionRecordRepository.findById(txId)).thenReturn(Optional.of(pendingTx));
		when(accountOperationsPort.lockAndLoad(targetAccountNumber)).thenReturn(Optional.of(targetAccount));
		when(accountOperationsPort.lockAndLoad(sourceAccountNumber)).thenReturn(Optional.of(sourceAccount));
		when(processedEventPort.saveIfAbsent(any())).thenReturn(true);

		processTransactionService.process(event);

		assertEquals(new BigDecimal("50.00"), sourceAccount.getBalance());
		assertEquals(new BigDecimal("70.00"), targetAccount.getBalance());
		verify(accountOperationsPort, times(1)).save(sourceAccount);
		verify(accountOperationsPort, times(1)).save(targetAccount);

		verify(transactionRecordRepository, times(1)).save(transactionCaptor.capture());
		TransactionRecord finalTx = transactionCaptor.getValue();
		assertEquals(txId, finalTx.getId());
		assertEquals(TransactionStatus.COMPLETED, finalTx.getStatus());

		verify(eventPublisher, times(1)).publish(eventCaptor.capture());
		TransactionCompletedEvent completedEvent = (TransactionCompletedEvent) eventCaptor.getValue();
		assertEquals(txId, completedEvent.transactionId());
	}

	@Test
	void shouldMarkTransactionAsFailedWhenFirstAccountNotFoundForTransfer() {
		String txId = "tx-transfer";
		String sourceAccountNumber = "A1B2C3D4E5";
		String targetAccountNumber = "X9Y8Z7W6V5";
		TransactionRecord pendingTx = TransactionRecord.reconstitute(txId, sourceAccountNumber, targetAccountNumber,
				new BigDecimal("50.00"), TransactionType.TRANSFER, Instant.now(), TransactionStatus.PENDING, null);
		TransactionPendingEvent event = new TransactionPendingEvent(UUID.randomUUID(), txId, Instant.now(),
				sourceAccountNumber, targetAccountNumber, new BigDecimal("50.00"), TransactionType.TRANSFER,
				"USER-123");

		when(transactionRecordRepository.findById(txId)).thenReturn(Optional.of(pendingTx));
		when(accountOperationsPort.lockAndLoad(sourceAccountNumber)).thenReturn(Optional.empty()); // first
																									// alphabetically
		when(processedEventPort.saveIfAbsent(any())).thenReturn(true);

		processTransactionService.process(event);

		verify(transactionRecordRepository, times(1)).save(transactionCaptor.capture());
		TransactionRecord finalTx = transactionCaptor.getValue();
		assertEquals(txId, finalTx.getId());
		assertEquals(TransactionStatus.FAILED, finalTx.getStatus());
		assertEquals("Account not found: " + sourceAccountNumber, finalTx.getFailureReason());

		verify(eventPublisher, times(1)).publish(eventCaptor.capture());
		TransactionFailedEvent failedEvent = (TransactionFailedEvent) eventCaptor.getValue();
		assertEquals(txId, failedEvent.transactionId());
		assertEquals("Account not found: " + sourceAccountNumber, failedEvent.failureReason());
	}

	@Test
	void shouldMarkTransactionAsFailedWhenSecondAccountNotFoundForTransfer() {
		String txId = "tx-transfer";
		String sourceAccountNumber = "A1B2C3D4E5";
		String targetAccountNumber = "X9Y8Z7W6V5";
		TransactionRecord pendingTx = TransactionRecord.reconstitute(txId, sourceAccountNumber, targetAccountNumber,
				new BigDecimal("50.00"), TransactionType.TRANSFER, Instant.now(), TransactionStatus.PENDING, null);
		TransactionPendingEvent event = new TransactionPendingEvent(UUID.randomUUID(), txId, Instant.now(),
				sourceAccountNumber, targetAccountNumber, new BigDecimal("50.00"), TransactionType.TRANSFER,
				"USER-123");
		Account sourceAccount = Account.reconstitute("uuid-1", "USER-123", sourceAccountNumber,
				new BigDecimal("100.00"));

		when(transactionRecordRepository.findById(txId)).thenReturn(Optional.of(pendingTx));
		when(accountOperationsPort.lockAndLoad(sourceAccountNumber)).thenReturn(Optional.of(sourceAccount)); // first
																												// alphabetically
		when(accountOperationsPort.lockAndLoad(targetAccountNumber)).thenReturn(Optional.empty()); // second
																									// alphabetically
		when(processedEventPort.saveIfAbsent(any())).thenReturn(true);

		processTransactionService.process(event);

		verify(transactionRecordRepository, times(1)).save(transactionCaptor.capture());
		TransactionRecord finalTx = transactionCaptor.getValue();
		assertEquals(txId, finalTx.getId());
		assertEquals(TransactionStatus.FAILED, finalTx.getStatus());
		assertEquals("Account not found: " + targetAccountNumber, finalTx.getFailureReason());

		verify(eventPublisher, times(1)).publish(eventCaptor.capture());
		TransactionFailedEvent failedEvent = (TransactionFailedEvent) eventCaptor.getValue();
		assertEquals(txId, failedEvent.transactionId());
		assertEquals("Account not found: " + targetAccountNumber, failedEvent.failureReason());
	}

	@Test
	void shouldIgnoreProcessingWhenTransactionAlreadyProcessed() {
		String txId = "tx-789";
		TransactionRecord completedTx = TransactionRecord.reconstitute(txId, null, "ACC123", new BigDecimal("100.00"),
				TransactionType.DEPOSIT, Instant.now(), TransactionStatus.COMPLETED, null);
		TransactionPendingEvent event = new TransactionPendingEvent(UUID.randomUUID(), txId, Instant.now(), null,
				"ACC123", new BigDecimal("100.00"), TransactionType.DEPOSIT, "USER-123");

		when(transactionRecordRepository.findById(txId)).thenReturn(Optional.of(completedTx));
		when(processedEventPort.saveIfAbsent(any())).thenReturn(true);

		processTransactionService.process(event);

		verify(accountOperationsPort, never()).lockAndLoad(anyString());
		verify(transactionRecordRepository, never()).save(any());
		verify(eventPublisher, never()).publish(any());
	}

	@Test
	void shouldRethrowTechnicalErrors() {
		String txId = "tx-123";
		String accountNumber = "A1B2C3D4E5";
		TransactionRecord pendingTx = TransactionRecord.reconstitute(txId, null, accountNumber,
				new BigDecimal("100.00"), TransactionType.DEPOSIT, Instant.now(), TransactionStatus.PENDING, null);
		TransactionPendingEvent event = new TransactionPendingEvent(UUID.randomUUID(), txId, Instant.now(), null,
				accountNumber, new BigDecimal("100.00"), TransactionType.DEPOSIT, "USER-123");

		when(transactionRecordRepository.findById(txId)).thenReturn(Optional.of(pendingTx));
		when(accountOperationsPort.lockAndLoad(accountNumber)).thenThrow(new RuntimeException("Database lock timeout"));
		when(processedEventPort.saveIfAbsent(any())).thenReturn(true);

		RuntimeException exception = assertThrows(RuntimeException.class,
				() -> processTransactionService.process(event));
		assertEquals("Database lock timeout", exception.getMessage());

		verify(transactionRecordRepository, never()).save(any());
		verify(eventPublisher, never()).publish(any());
	}

	@Test
	void shouldIgnoreDuplicateEventWhenProcessedEventPortReturnsFalse() {
		String txId = "tx-123";
		TransactionPendingEvent event = new TransactionPendingEvent(UUID.randomUUID(), txId, Instant.now(), null,
				"ACC123", new BigDecimal("100.00"), TransactionType.DEPOSIT, "USER-123");

		when(processedEventPort.saveIfAbsent(event.eventId())).thenReturn(false);

		processTransactionService.process(event);

		verify(transactionRecordRepository, never()).findById(anyString());
		verify(accountOperationsPort, never()).lockAndLoad(anyString());
		verify(transactionRecordRepository, never()).save(any());
		verify(eventPublisher, never()).publish(any());
	}
}
