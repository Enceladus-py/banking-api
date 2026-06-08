package com.example.demo.transaction.infrastructure.adapter.in.web;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.common.application.port.in.dto.PageRequest;
import com.example.demo.common.application.port.in.dto.PageResult;
import com.example.demo.common.domain.exception.EntityNotFoundException;
import com.example.demo.common.infrastructure.security.CustomUserDetails;
import com.example.demo.common.infrastructure.security.SecurityConfig;
import com.example.demo.transaction.application.port.in.DepositMoneyUseCase;
import com.example.demo.transaction.application.port.in.DepositMoneyUseCase.DepositCommand;
import com.example.demo.transaction.application.port.in.GetAccountTransactionsUseCase;
import com.example.demo.transaction.application.port.in.GetTransactionUseCase;
import com.example.demo.transaction.application.port.in.WithdrawMoneyUseCase;
import com.example.demo.transaction.domain.model.TransactionRecord;

@WebMvcTest(TransactionController.class)
@Import(SecurityConfig.class)
class TransactionControllerTest {
	@MockitoBean
	private DepositMoneyUseCase depositMoneyUseCase;

	@MockitoBean
	private WithdrawMoneyUseCase withdrawMoneyUseCase;

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private GetAccountTransactionsUseCase getAccountTransactionsUseCase;

	@MockitoBean
	private GetTransactionUseCase getTransactionUseCase;

	@MockitoBean
	private com.example.demo.common.infrastructure.security.JwtService jwtService;

	@MockitoBean
	private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

	@Test
	void shouldReturnPaginatedTransactionsWithCustomParams() throws Exception {
		// Arrange
		String accountNumber = "1234567890";
		String requesterId = "USER-123";
		PageResult<?> emptyResult = new PageResult<>(Collections.emptyList(), 2, 5, 10, 2);

		when(getAccountTransactionsUseCase.getTransactions(eq(accountNumber), any(PageRequest.class), eq(requesterId)))
				.thenAnswer(invocation -> emptyResult);

		// Act & Assert
		mockMvc.perform(get("/api/transactions/{accountNumber}?page=2&size=5", accountNumber)
				.with(user(new CustomUserDetails(requesterId, "test@test.com", "pass", Collections.emptyList()))) // Added
				// the
				// authentication
				// header
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.pageNumber").value(2)).andExpect(jsonPath("$.pageSize").value(5))
				.andExpect(jsonPath("$.totalElements").value(10));

		// Verify the Controller passed the exact HTTP params AND the user ID to the Use
		// Case
		verify(getAccountTransactionsUseCase).getTransactions(accountNumber, new PageRequest(2, 5), requesterId);
	}

	@Test
	void shouldReturnPaginatedTransactionsWithDefaultParams() throws Exception {
		// Arrange
		String accountNumber = "1234567890";
		String requesterId = "USER-123";
		PageResult<?> defaultResult = new PageResult<>(Collections.emptyList(), 0, 10, 0, 0);

		when(getAccountTransactionsUseCase.getTransactions(eq(accountNumber), any(PageRequest.class), eq(requesterId)))
				.thenAnswer(invocation -> defaultResult);

		// Act & Assert
		// We omit ?page= and ?size=
		mockMvc.perform(get("/api/transactions/{accountNumber}", accountNumber)
				.with(user(new CustomUserDetails(requesterId, "test@test.com", "pass", Collections.emptyList()))) // Added
				// the
				// authentication
				// header
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

		// Verify the Controller used the default values defined in @RequestParam AND
		// extracted the user ID
		verify(getAccountTransactionsUseCase).getTransactions(accountNumber, new PageRequest(0, 10), requesterId);
	}

	@Test
	void shouldReturn200AndTransactionResponseWhenGettingById() throws Exception {
		String txId = "tx-12345";
		String requesterId = "USER-123";
		com.example.demo.transaction.domain.model.TransactionRecord tx = new com.example.demo.transaction.domain.model.TransactionRecord(
				txId, "ACC-SRC", "ACC-TGT", new java.math.BigDecimal("50.00"),
				com.example.demo.transaction.domain.model.TransactionRecord.TransactionType.TRANSFER,
				java.time.LocalDateTime.now(),
				com.example.demo.transaction.domain.model.TransactionRecord.TransactionStatus.COMPLETED, null);

		when(getTransactionUseCase.getTransaction(eq(txId), eq(requesterId))).thenReturn(tx);

		mockMvc.perform(get("/api/transactions/id/{transactionId}", txId)
				.with(user(new CustomUserDetails(requesterId, "test@test.com", "pass", Collections.emptyList())))
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(txId)).andExpect(jsonPath("$.status").value("COMPLETED"))
				.andExpect(jsonPath("$.amount").value(50.00));

		verify(getTransactionUseCase).getTransaction(txId, requesterId);
	}
	@Test
	void shouldReturn200WhenDepositIsSuccessful() throws Exception {
		// Arrange
		String accountNumber = "A1B2C3D4E5";
		String requesterId = "USER-123";
		TransactionRecord updatedTx = new TransactionRecord("tx-id", null, accountNumber, new BigDecimal("100.00"),
				TransactionRecord.TransactionType.DEPOSIT, java.time.LocalDateTime.now(),
				TransactionRecord.TransactionStatus.PENDING, null);

		when(depositMoneyUseCase.deposit(any(DepositCommand.class))).thenReturn(updatedTx);

		String jsonPayload = """
				{
				    "accountNumber": "A1B2C3D4E5",
				    "amount": 100.00
				}
				""";

		// Act & Assert
		mockMvc.perform(post("/api/transactions/deposit")
				.with(user(new CustomUserDetails(requesterId, "test@test.com", "pass", Collections.emptyList()))) // Added
																													// Security
																													// Header
				.contentType(MediaType.APPLICATION_JSON).content(jsonPayload)).andExpect(status().isAccepted())
				.andExpect(jsonPath("$.targetAccountNumber").value(accountNumber))
				.andExpect(jsonPath("$.amount").value(100.00)).andExpect(jsonPath("$.status").value("PENDING"));

		verify(depositMoneyUseCase, times(1)).deposit(any(DepositCommand.class));
	}

	@Test
	void shouldReturn400BadRequestWhenDepositAmountIsNegative() throws Exception {
		// Arrange
		String jsonPayload = """
				{
				    "accountNumber": "A1B2C3D4E5",
				    "amount": -25.50
				}
				""";

		// Act & Assert
		mockMvc.perform(post("/api/transactions/deposit")
				.with(user(new CustomUserDetails("USER-123", "test@test.com", "pass", Collections.emptyList())))
				.contentType(MediaType.APPLICATION_JSON).content(jsonPayload)).andExpect(status().isBadRequest()); // Blocked
																													// by
																													// Web
																													// DTO
																													// Validation
																													// (@Positive)

		// Ensure the application core was never invoked
		verify(depositMoneyUseCase, never()).deposit(any(DepositCommand.class));
	}

	@Test
	void shouldReturn400BadRequestWhenAccountNumberIsBlank() throws Exception {
		// Arrange
		String jsonPayload = """
				{
				    "accountNumber": "",
				    "amount": 50.00
				}
				""";

		// Act & Assert
		mockMvc.perform(post("/api/transactions/deposit")
				.with(user(new CustomUserDetails("USER-123", "test@test.com", "pass", Collections.emptyList())))
				.contentType(MediaType.APPLICATION_JSON).content(jsonPayload)).andExpect(status().isBadRequest()); // Blocked
																													// by
																													// Web
																													// DTO
																													// Validation
																													// (@NotBlank)

		verify(depositMoneyUseCase, never()).deposit(any(DepositCommand.class));
	}

	@Test
	void shouldReturn403ForbiddenWhenUserAttemptsToTouchAnotherUsersAccount() throws Exception {
		// Arrange: Simulate the domain rejecting the action based on ownership
		when(depositMoneyUseCase.deposit(any(DepositCommand.class)))
				.thenThrow(new SecurityException("You are not authorized to deposit into this account"));

		String jsonPayload = """
				{
				    "accountNumber": "A1B2C3D4E5",
				    "amount": 100.00
				}
				""";

		// Act & Assert
		mockMvc.perform(post("/api/transactions/deposit")
				.with(user(new CustomUserDetails("HACKER-ID", "test@test.com", "pass", Collections.emptyList())))
				.contentType(MediaType.APPLICATION_JSON).content(jsonPayload)).andExpect(status().isForbidden()) // Ensure
																													// GlobalExceptionHandler
																													// maps
																													// SecurityException
																													// to
																													// 403
				.andExpect(jsonPath("$.message").value("You are not authorized to deposit into this account"));
	}

	@Test
	void shouldReturn404NotFoundWhenAccountNumberDoesNotExist() throws Exception {
		// Arrange: Tell the use case to throw an exception when called
		when(depositMoneyUseCase.deposit(any(DepositCommand.class)))
				.thenThrow(new EntityNotFoundException("Account not found"));

		String jsonPayload = """
				{
				    "accountNumber": "0000000000",
				    "amount": 100.00
				}
				""";

		// Act & Assert
		mockMvc.perform(post("/api/transactions/deposit")
				.with(user(new CustomUserDetails("USER-123", "test@test.com", "pass", Collections.emptyList())))
				.contentType(MediaType.APPLICATION_JSON).content(jsonPayload)).andExpect(status().isNotFound()); // We
																													// expect
																													// an
																													// HTTP
																													// 404
	}

	@Test
	void shouldReturn200AndUpdatedAccountOnSuccessfulWithdrawal() throws Exception {
		// Arrange
		String accountNumber = "1122334455";
		String requesterId = "USER-123";
		TransactionRecord expectedTx = new TransactionRecord("tx-id", accountNumber, null, new BigDecimal("150.00"),
				TransactionRecord.TransactionType.WITHDRAWAL, java.time.LocalDateTime.now(),
				TransactionRecord.TransactionStatus.PENDING, null);

		when(withdrawMoneyUseCase.withdraw(any(WithdrawMoneyUseCase.WithdrawCommand.class))).thenReturn(expectedTx);

		String validPayload = """
				{
				    "accountNumber": "1122334455",
				    "amount": 150.00
				}
				""";

		// Act & Assert
		mockMvc.perform(post("/api/transactions/withdraw")
				.with(user(new CustomUserDetails(requesterId, "test@test.com", "pass", Collections.emptyList())))
				.contentType(MediaType.APPLICATION_JSON).content(validPayload)).andExpect(status().isAccepted())
				.andExpect(jsonPath("$.sourceAccountNumber").value(accountNumber))
				.andExpect(jsonPath("$.amount").value(150.00)).andExpect(jsonPath("$.status").value("PENDING"));
	}

	@Test
	void shouldReturn400BadRequestWhenInsufficientFunds() throws Exception {
		// Arrange: Simulate the domain rejecting the withdrawal
		when(withdrawMoneyUseCase.withdraw(any(WithdrawMoneyUseCase.WithdrawCommand.class)))
				.thenThrow(new IllegalStateException("Insufficient funds"));

		String overDraftPayload = """
				{
				    "accountNumber": "1122334455",
				    "amount": 9000.00
				}
				""";

		// Act & Assert: GlobalExceptionHandler should map this to HTTP 400
		mockMvc.perform(post("/api/transactions/withdraw")
				.with(user(new CustomUserDetails("USER-123", "test@test.com", "pass", Collections.emptyList())))
				.contentType(MediaType.APPLICATION_JSON).content(overDraftPayload)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Insufficient funds"));
	}

	@Test
	void shouldReturn400BadRequestWhenWithdrawalPayloadIsInvalid() throws Exception {
		// Arrange: Negative amount violates @Positive
		String invalidPayload = """
				{
				    "accountNumber": "1122334455",
				    "amount": -20.00
				}
				""";

		// Act & Assert
		mockMvc.perform(post("/api/transactions/withdraw")
				.with(user(new CustomUserDetails("USER-123", "test@test.com", "pass", Collections.emptyList())))
				.contentType(MediaType.APPLICATION_JSON).content(invalidPayload)).andExpect(status().isBadRequest());

		// Ensure the core business logic was never touched
		verify(withdrawMoneyUseCase, never()).withdraw(any(WithdrawMoneyUseCase.WithdrawCommand.class));
	}
}
