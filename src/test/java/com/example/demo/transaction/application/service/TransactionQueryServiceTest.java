package com.example.demo.transaction.application.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.account.application.port.in.AccountOperationsPort;
import com.example.demo.account.domain.model.Account;
import com.example.demo.common.application.port.in.dto.PageRequest;
import com.example.demo.common.application.port.in.dto.PageResult;
import com.example.demo.common.domain.exception.AccessDeniedException;
import com.example.demo.transaction.application.port.out.TransactionRecordRepository;
import com.example.demo.transaction.domain.model.TransactionRecord;

@ExtendWith(MockitoExtension.class)
class TransactionQueryServiceTest {

	@Mock
	private TransactionRecordRepository transactionRecordRepository;
	@Mock
	private AccountOperationsPort accountOperationsPort;

	@InjectMocks
	private TransactionQueryService service;

	@Test
	void shouldRetrieveTransactionsWhenAuthorized() {
		Account account = Account.createNew("USER-1", "ACC-123456");
		PageRequest pageRequest = new PageRequest(0, 10);

		when(accountOperationsPort.findByAccountNumber("ACC-123456")).thenReturn(Optional.of(account));
		when(transactionRecordRepository.findByAccountNumber("ACC-123456", pageRequest))
				.thenReturn(new PageResult<>(Collections.emptyList(), 0, 10, 0, 0));

		service.getTransactions("ACC-123456", pageRequest, "USER-1");

		verify(transactionRecordRepository).findByAccountNumber("ACC-123456", pageRequest);
	}

	@Test
	void shouldBlockRetrievingTransactionsForUnauthorizedUser() {
		Account account = Account.createNew("USER-1", "ACC-123456");
		PageRequest pageRequest = new PageRequest(0, 10);

		when(accountOperationsPort.findByAccountNumber("ACC-123456")).thenReturn(Optional.of(account));

		assertThrows(AccessDeniedException.class, () -> service.getTransactions("ACC-123456", pageRequest, "HACKER"));

		verify(transactionRecordRepository, never()).findByAccountNumber(anyString(), any());
	}

	@Test
	void shouldRetrieveTransactionByIdWhenAuthorizedAsSource() {
		TransactionRecord tx = TransactionRecord.reconstitute("tx-123", "1234567890", "0987654321",
				new BigDecimal("100"), TransactionRecord.TransactionType.TRANSFER, java.time.Instant.now(),
				TransactionRecord.TransactionStatus.PENDING, null);
		Account sourceAccount = Account.createNew("USER-1", "1234567890");
		Account targetAccount = Account.createNew("USER-2", "0987654321");

		when(transactionRecordRepository.findById("tx-123")).thenReturn(Optional.of(tx));
		when(accountOperationsPort.findByAccountNumber("1234567890")).thenReturn(Optional.of(sourceAccount));
		when(accountOperationsPort.findByAccountNumber("0987654321")).thenReturn(Optional.of(targetAccount));

		TransactionRecord result = service.getTransaction("tx-123", "USER-1");

		org.junit.jupiter.api.Assertions.assertEquals("tx-123", result.getId());
	}

	@Test
	void shouldRetrieveTransactionByIdWhenAuthorizedAsTarget() {
		TransactionRecord tx = TransactionRecord.reconstitute("tx-123", "1234567890", "0987654321",
				new BigDecimal("100"), TransactionRecord.TransactionType.TRANSFER, java.time.Instant.now(),
				TransactionRecord.TransactionStatus.PENDING, null);
		Account sourceAccount = Account.createNew("USER-1", "1234567890");
		Account targetAccount = Account.createNew("USER-2", "0987654321");

		when(transactionRecordRepository.findById("tx-123")).thenReturn(Optional.of(tx));
		when(accountOperationsPort.findByAccountNumber("1234567890")).thenReturn(Optional.of(sourceAccount));
		when(accountOperationsPort.findByAccountNumber("0987654321")).thenReturn(Optional.of(targetAccount));

		TransactionRecord result = service.getTransaction("tx-123", "USER-2");

		org.junit.jupiter.api.Assertions.assertEquals("tx-123", result.getId());
	}

	@Test
	void shouldBlockRetrievingTransactionByIdForUnauthorizedUser() {
		TransactionRecord tx = TransactionRecord.reconstitute("tx-123", "1234567890", "0987654321",
				new BigDecimal("100"), TransactionRecord.TransactionType.TRANSFER, java.time.Instant.now(),
				TransactionRecord.TransactionStatus.PENDING, null);
		Account sourceAccount = Account.createNew("USER-1", "1234567890");
		Account targetAccount = Account.createNew("USER-2", "0987654321");

		when(transactionRecordRepository.findById("tx-123")).thenReturn(Optional.of(tx));
		when(accountOperationsPort.findByAccountNumber("1234567890")).thenReturn(Optional.of(sourceAccount));
		when(accountOperationsPort.findByAccountNumber("0987654321")).thenReturn(Optional.of(targetAccount));

		assertThrows(AccessDeniedException.class, () -> service.getTransaction("tx-123", "HACKER"));
	}

	@Test
	void shouldBlockRetrievingTransactionByIdWhenAccountsAreDeleted() {
		TransactionRecord tx = TransactionRecord.reconstitute("tx-123", "1234567890", "0987654321",
				new BigDecimal("100"), TransactionRecord.TransactionType.TRANSFER, java.time.Instant.now(),
				TransactionRecord.TransactionStatus.PENDING, null);

		when(transactionRecordRepository.findById("tx-123")).thenReturn(Optional.of(tx));
		when(accountOperationsPort.findByAccountNumber("1234567890")).thenReturn(Optional.empty());
		when(accountOperationsPort.findByAccountNumber("0987654321")).thenReturn(Optional.empty());

		assertThrows(AccessDeniedException.class, () -> service.getTransaction("tx-123", "USER-1"));
	}

	@Test
	void shouldThrowEntityNotFoundWhenAccountNotFoundForGetTransactions() {
		PageRequest pageRequest = new PageRequest(0, 10);
		when(accountOperationsPort.findByAccountNumber("ACC-MISSING")).thenReturn(Optional.empty());

		assertThrows(com.example.demo.common.domain.exception.EntityNotFoundException.class,
				() -> service.getTransactions("ACC-MISSING", pageRequest, "USER-1"));
	}

	@Test
	void shouldThrowEntityNotFoundWhenTransactionNotFound() {
		when(transactionRecordRepository.findById("tx-missing")).thenReturn(Optional.empty());

		assertThrows(com.example.demo.common.domain.exception.EntityNotFoundException.class,
				() -> service.getTransaction("tx-missing", "USER-1"));
	}

	@Test
	void shouldRetrieveDepositTransactionWhenAuthorizedAsTarget() {
		TransactionRecord tx = TransactionRecord.reconstitute("tx-123", null, "0987654321", new BigDecimal("100"),
				TransactionRecord.TransactionType.DEPOSIT, java.time.Instant.now(),
				TransactionRecord.TransactionStatus.PENDING, null);
		Account targetAccount = Account.createNew("USER-2", "0987654321");

		when(transactionRecordRepository.findById("tx-123")).thenReturn(Optional.of(tx));
		when(accountOperationsPort.findByAccountNumber("0987654321")).thenReturn(Optional.of(targetAccount));

		TransactionRecord result = service.getTransaction("tx-123", "USER-2");

		org.junit.jupiter.api.Assertions.assertEquals("tx-123", result.getId());
	}

	@Test
	void shouldRetrieveWithdrawalTransactionWhenAuthorizedAsSource() {
		TransactionRecord tx = TransactionRecord.reconstitute("tx-123", "1234567890", null, new BigDecimal("100"),
				TransactionRecord.TransactionType.WITHDRAWAL, java.time.Instant.now(),
				TransactionRecord.TransactionStatus.PENDING, null);
		Account sourceAccount = Account.createNew("USER-1", "1234567890");

		when(transactionRecordRepository.findById("tx-123")).thenReturn(Optional.of(tx));
		when(accountOperationsPort.findByAccountNumber("1234567890")).thenReturn(Optional.of(sourceAccount));

		TransactionRecord result = service.getTransaction("tx-123", "USER-1");

		org.junit.jupiter.api.Assertions.assertEquals("tx-123", result.getId());
	}
}
