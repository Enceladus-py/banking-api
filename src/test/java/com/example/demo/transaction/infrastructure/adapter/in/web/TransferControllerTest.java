package com.example.demo.transaction.infrastructure.adapter.in.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.account.domain.exception.InsufficientFundsException;
import com.example.demo.common.domain.exception.EntityNotFoundException;
import com.example.demo.common.infrastructure.security.CustomUserDetails;
import com.example.demo.common.infrastructure.security.SecurityConfig;
import com.example.demo.transaction.application.port.in.TransferMoneyUseCase;
import com.example.demo.transaction.domain.model.TransactionRecord;

@WebMvcTest(TransferController.class)
@Import(SecurityConfig.class)
class TransferControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private TransferMoneyUseCase transferMoneyUseCase;

	@MockitoBean
	private com.example.demo.common.infrastructure.security.JwtService jwtService;

	@MockitoBean
	private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

	@Test
	void shouldReturn200OnSuccessfulTransfer() throws Exception {
		String validPayload = """
				{
				    "sourceAccountNumber": "1111111111",
				    "targetAccountNumber": "2222222222",
				    "amount": 100.50
				}
				""";
		String requesterId = "USER-123";

		TransactionRecord expectedTx = TransactionRecord.reconstitute("tx-id", "1111111111", "2222222222",
				new BigDecimal("100.50"), TransactionRecord.TransactionType.TRANSFER, java.time.LocalDateTime.now(),
				TransactionRecord.TransactionStatus.PENDING, null);
		when(transferMoneyUseCase.transfer(any(TransferMoneyUseCase.TransferCommand.class))).thenReturn(expectedTx);

		mockMvc.perform(post("/api/transfers")
				.with(user(new CustomUserDetails(requesterId, "test@test.com", "pass", Collections.emptyList()))) // Added
																													// Security
																													// Header
				.contentType(MediaType.APPLICATION_JSON).content(validPayload)).andExpect(status().isAccepted());

		// Capture and verify the command mapping
		ArgumentCaptor<TransferMoneyUseCase.TransferCommand> commandCaptor = ArgumentCaptor
				.forClass(TransferMoneyUseCase.TransferCommand.class);
		verify(transferMoneyUseCase).transfer(commandCaptor.capture());

		TransferMoneyUseCase.TransferCommand capturedCommand = commandCaptor.getValue();
		assertEquals(requesterId, capturedCommand.requesterId());
		assertEquals("1111111111", capturedCommand.sourceAccountNumber());
	}

	@Test
	void shouldReturn401WhenUnauthenticated() throws Exception {
		String validPayload = """
				{
				    "sourceAccountNumber": "1111111111",
				    "targetAccountNumber": "2222222222",
				    "amount": 100.50
				}
				""";

		// Attempting to call the endpoint without the authentication header
		mockMvc.perform(post("/api/transfers").contentType(MediaType.APPLICATION_JSON).content(validPayload))
				.andExpect(status().isUnauthorized());

		verify(transferMoneyUseCase, never()).transfer(any());
	}

	@Test
	void shouldReturn403ForbiddenWhenUserAttemptsTransferFromAnotherUsersAccount() throws Exception {
		String payload = """
				{
				    "sourceAccountNumber": "1111111111",
				    "targetAccountNumber": "2222222222",
				    "amount": 50.00
				}
				""";

		when(transferMoneyUseCase.transfer(any(TransferMoneyUseCase.TransferCommand.class)))
				.thenThrow(new SecurityException("You are not authorized to transfer money from this account"));

		mockMvc.perform(post("/api/transfers")
				.with(user(new CustomUserDetails("HACKER-ID", "test@test.com", "pass", Collections.emptyList())))
				.contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("You are not authorized to transfer money from this account"));
	}

	@Test
	void shouldReturn400WhenDomainThrowsInsufficientFunds() throws Exception {
		String overdraftPayload = """
				{
				    "sourceAccountNumber": "1111111111",
				    "targetAccountNumber": "2222222222",
				    "amount": 9000.00
				}
				""";

		when(transferMoneyUseCase.transfer(any(TransferMoneyUseCase.TransferCommand.class)))
				.thenThrow(new InsufficientFundsException("Insufficient funds"));

		mockMvc.perform(post("/api/transfers")
				.with(user(new CustomUserDetails("USER-123", "test@test.com", "pass", Collections.emptyList())))
				.contentType(MediaType.APPLICATION_JSON).content(overdraftPayload)).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Insufficient funds"));
	}

	@Test
	void shouldReturn404WhenTargetAccountIsNotFound() throws Exception {
		String payload = """
				{
				    "sourceAccountNumber": "1111111111",
				    "targetAccountNumber": "9999999999",
				    "amount": 100.00
				}
				""";

		// The domain/service layer throws EntityNotFoundException
		when(transferMoneyUseCase.transfer(any(TransferMoneyUseCase.TransferCommand.class)))
				.thenThrow(new EntityNotFoundException("Target account not found"));

		mockMvc.perform(post("/api/transfers")
				.with(user(new CustomUserDetails("USER-123", "test@test.com", "pass", Collections.emptyList())))
				.contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isNotFound()); // Mapped
																												// by
																												// GlobalExceptionHandler
	}

	@Test
	void shouldReturn400WhenPayloadViolatesValidation() throws Exception {
		// Negative amount violates Web DTO @Positive constraint
		String invalidPayload = """
				{
				    "sourceAccountNumber": "1111111111",
				    "targetAccountNumber": "2222222222",
				    "amount": -50.00
				}
				""";

		mockMvc.perform(post("/api/transfers")
				.with(user(new CustomUserDetails("USER-123", "test@test.com", "pass", Collections.emptyList())))
				.contentType(MediaType.APPLICATION_JSON).content(invalidPayload)).andExpect(status().isBadRequest());

		// The core business logic should be protected from bad data
		verify(transferMoneyUseCase, never()).transfer(any(TransferMoneyUseCase.TransferCommand.class));
	}
}
