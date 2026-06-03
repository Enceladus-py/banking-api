package com.example.demo.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.application.port.in.TransferMoneyUseCase.TransferCommand;
import com.example.demo.application.port.out.AccountRepository;
import com.example.demo.application.port.out.EventPublisher;
import com.example.demo.application.port.out.TransactionRecordRepository;
import com.example.demo.domain.event.TransactionPendingEvent;
import com.example.demo.domain.exception.EntityNotFoundException;
import com.example.demo.domain.model.Account;
import com.example.demo.domain.model.TransactionRecord;
import com.example.demo.domain.model.TransactionRecord.TransactionStatus;
import com.example.demo.domain.model.TransactionRecord.TransactionType;

/**
 * Unit tests for {@link TransferService}.
 *
 * <p>
 * TransferService now follows the outbox pattern: it validates accounts,
 * creates a PENDING ledger entry, and publishes a
 * {@link TransactionPendingEvent}. The actual balance mutation is done
 * asynchronously by {@link ProcessTransactionService} when the outbox scheduler
 * fires.
 */
@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private TransactionRecordRepository transactionRecordRepository;

	@Mock
	private EventPublisher eventPublisher;

	private TransferService transferService;

	@Captor
	private ArgumentCaptor<TransactionRecord> transactionCaptor;

	@Captor
	private ArgumentCaptor<TransactionPendingEvent> eventCaptor;

	@BeforeEach
	void setUp() {
		transferService = new TransferService(accountRepository, transactionRecordRepository, eventPublisher);
	}

	@Test
	void shouldCreatePendingLedgerEntryAndPublishEventOnSuccessfulTransfer() {
		// Arrange
		String sourceId = "SRC1234567";
		String targetId = "TGT1234567";
		String requesterId = "USER-1";
		BigDecimal amount = new BigDecimal("150.00");

		Account sourceAccount = new Account("uuid-1", requesterId, sourceId, new BigDecimal("500.00"), 1L);
		Account targetAccount = new Account("uuid-2", "USER-2", targetId, new BigDecimal("100.00"), 1L);

		when(accountRepository.findByAccountNumber(sourceId)).thenReturn(Optional.of(sourceAccount));
		when(accountRepository.findByAccountNumber(targetId)).thenReturn(Optional.of(targetAccount));

		// Act
		transferService.transfer(new TransferCommand(sourceId, targetId, amount, requesterId));

		// Assert: balances are NOT mutated by the initiating service
		assertEquals(new BigDecimal("500.00"), sourceAccount.getBalance(),
				"Source balance must not change during initiation — outbox handles it");
		assertEquals(new BigDecimal("100.00"), targetAccount.getBalance(),
				"Target balance must not change during initiation — outbox handles it");

		// Assert: no account saves during initiation
		verify(accountRepository, never()).save(any());

		// Assert: PENDING ledger entry is saved
		verify(transactionRecordRepository).save(transactionCaptor.capture());
		TransactionRecord pendingTx = transactionCaptor.getValue();
		assertEquals(sourceId, pendingTx.getSourceAccountNumber());
		assertEquals(targetId, pendingTx.getTargetAccountNumber());
		assertEquals(amount, pendingTx.getAmount());
		assertEquals(TransactionType.TRANSFER, pendingTx.getType());
		assertEquals(TransactionStatus.PENDING, pendingTx.getStatus());

		// Assert: TransactionPendingEvent is published to outbox
		verify(eventPublisher).publish(eventCaptor.capture());
		TransactionPendingEvent event = eventCaptor.getValue();
		assertEquals(pendingTx.getId(), event.transactionId());
		assertEquals(TransactionType.TRANSFER, event.type());
		assertEquals(sourceId, event.sourceAccountNumber());
		assertEquals(targetId, event.targetAccountNumber());
		assertEquals(amount, event.amount());
	}

	@Test
	void shouldAbortWhenHackerAttemptsTransfer() {
		// Arrange
		String sourceId = "SRC1234567";
		String targetId = "TGT1234567";
		Account sourceAccount = new Account("uuid-1", "USER-1", sourceId, new BigDecimal("500.00"), 1L);

		when(accountRepository.findByAccountNumber(sourceId)).thenReturn(Optional.of(sourceAccount));

		// Act & Assert
		SecurityException ex = assertThrows(SecurityException.class, () -> transferService
				.transfer(new TransferCommand(sourceId, targetId, new BigDecimal("100.00"), "HACKER-ID")));

		assertEquals("You are not authorized to transfer money from this account", ex.getMessage());
		verify(transactionRecordRepository, never()).save(any());
		verify(eventPublisher, never()).publish(any());
	}

	@Test
	void shouldAbortWhenTargetAccountNotFound() {
		// Arrange
		String sourceId = "SRC1234567";
		String targetId = "TGT1234567";
		String requesterId = "USER-1";
		Account sourceAccount = new Account("uuid-1", requesterId, sourceId, new BigDecimal("500.00"), 1L);

		when(accountRepository.findByAccountNumber(sourceId)).thenReturn(Optional.of(sourceAccount));
		when(accountRepository.findByAccountNumber(targetId)).thenReturn(Optional.empty());

		// Act & Assert
		EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> transferService
				.transfer(new TransferCommand(sourceId, targetId, new BigDecimal("100.00"), requesterId)));

		assertEquals("Target account not found", ex.getMessage());
		verify(transactionRecordRepository, never()).save(any());
		verify(eventPublisher, never()).publish(any());
	}

	@Test
	void shouldAbortWhenSourceAccountNotFound() {
		// Arrange
		String sourceId = "SRC1234567";
		String targetId = "TGT1234567";

		when(accountRepository.findByAccountNumber(sourceId)).thenReturn(Optional.empty());

		// Act & Assert
		EntityNotFoundException ex = assertThrows(EntityNotFoundException.class, () -> transferService
				.transfer(new TransferCommand(sourceId, targetId, new BigDecimal("100.00"), "USER-1")));

		assertEquals("Source account not found", ex.getMessage());
		verify(transactionRecordRepository, never()).save(any());
		verify(eventPublisher, never()).publish(any());
	}
}
