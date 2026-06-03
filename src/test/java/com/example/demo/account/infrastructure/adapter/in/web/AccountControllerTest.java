package com.example.demo.account.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.account.application.port.in.CreateAccountUseCase;
import com.example.demo.account.application.port.in.CreateAccountUseCase.CreateAccountCommand;
import com.example.demo.account.application.port.in.GetAccountUseCase;
import com.example.demo.account.domain.model.Account;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

	@Autowired
	private MockMvc mockMvc; // Simulates HTTP requests

	@MockitoBean
	private CreateAccountUseCase createAccountUseCase; // Fakes the inner hexagon

	@MockitoBean
	private GetAccountUseCase getAccountUseCase;

	@Test
	void shouldReturn200WhenAccountIsCreated() throws Exception {
		// 1. Arrange: Prepare the mock response from the Domain
		Account mockDomainAccount = new Account("uuid-1", "USER-123", "ACC1234567", BigDecimal.ZERO, 1L);
		when(createAccountUseCase.createAccount(any(CreateAccountCommand.class))).thenReturn(mockDomainAccount);

		// 2 & 3. Act & Assert: Send request with ONLY the User ID header (no JSON body
		// needed anymore)
		mockMvc.perform(post("/api/accounts").header("X-User-Id", "USER-123")).andExpect(status().isOk()) // HTTP 200 OK
				.andExpect(jsonPath("$.id").value("uuid-1")).andExpect(jsonPath("$.ownerId").value("USER-123"))
				.andExpect(jsonPath("$.accountNumber").value("ACC1234567"))
				.andExpect(jsonPath("$.balance").value(0.00));
	}

	@Test
	void shouldReturn400BadRequestWhenUserIdHeaderIsMissing() throws Exception {
		// Act & Assert: Attempting to call the endpoint without the authentication
		// header
		mockMvc.perform(post("/api/accounts")).andExpect(status().isBadRequest()); // Spring automatically blocks
																					// requests missing
																					// required headers

		// Ensure use case is never called
		verify(createAccountUseCase, never()).createAccount(any());
	}

	@Test
	void shouldReturn200AndAccountDetailsWhenGettingAccount() throws Exception {
		String accountNumber = "1234567890";
		String requesterId = "USER-123";
		Account mockAccount = new Account("uuid-1", requesterId, accountNumber, new BigDecimal("250.00"), 1L);

		when(getAccountUseCase.getAccount(accountNumber, requesterId)).thenReturn(mockAccount);

		mockMvc.perform(get("/api/accounts/{accountNumber}", accountNumber).header("X-User-Id", requesterId))
				.andExpect(status().isOk()).andExpect(jsonPath("$.id").value("uuid-1"))
				.andExpect(jsonPath("$.ownerId").value("USER-123"))
				.andExpect(jsonPath("$.accountNumber").value("1234567890"))
				.andExpect(jsonPath("$.balance").value(250.00));

		verify(getAccountUseCase).getAccount(accountNumber, requesterId);
	}
}
