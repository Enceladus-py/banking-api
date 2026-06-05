package com.example.demo.account.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.account.application.port.in.CreateAccountUseCase;
import com.example.demo.account.application.port.in.CreateAccountUseCase.CreateAccountCommand;
import com.example.demo.account.application.port.in.GetAccountUseCase;
import com.example.demo.account.application.port.in.GetAccountsUseCase;
import com.example.demo.account.domain.model.Account;
import com.example.demo.common.security.CustomUserDetails;
import com.example.demo.common.security.SecurityConfig;

@WebMvcTest(AccountController.class)
@Import(SecurityConfig.class)
class AccountControllerTest {

	@Autowired
	private MockMvc mockMvc; // Simulates HTTP requests

	@MockitoBean
	private CreateAccountUseCase createAccountUseCase; // Fakes the inner hexagon

	@MockitoBean
	private GetAccountUseCase getAccountUseCase;

	@MockitoBean
	private GetAccountsUseCase getAccountsUseCase;

	@MockitoBean
	private com.example.demo.common.security.JwtService jwtService;

	@MockitoBean
	private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

	@Test
	void shouldReturn200WhenAccountIsCreated() throws Exception {
		// 1. Arrange: Prepare the mock response from the Domain
		Account mockDomainAccount = new Account("uuid-1", "USER-123", "ACC1234567", BigDecimal.ZERO, 1L);
		when(createAccountUseCase.createAccount(any(CreateAccountCommand.class))).thenReturn(mockDomainAccount);

		// 2 & 3. Act & Assert: Send request with authenticated user
		mockMvc.perform(post("/api/accounts")
				.with(user(new CustomUserDetails("USER-123", "test@test.com", "pass", Collections.emptyList()))))
				.andExpect(status().isOk()) // HTTP 200 OK
				.andExpect(jsonPath("$.id").value("uuid-1")).andExpect(jsonPath("$.ownerId").value("USER-123"))
				.andExpect(jsonPath("$.accountNumber").value("ACC1234567"))
				.andExpect(jsonPath("$.balance").value(0.00));
	}

	@Test
	void shouldReturn403WhenUnauthenticated() throws Exception {
		// Act & Assert: Attempting to call the endpoint without the authentication
		// header
		mockMvc.perform(post("/api/accounts")).andExpect(status().isForbidden()); // Spring Security automatically
																					// blocks
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

		mockMvc.perform(get("/api/accounts/{accountNumber}", accountNumber)
				.with(user(new CustomUserDetails(requesterId, "test@test.com", "pass", Collections.emptyList()))))
				.andExpect(status().isOk()).andExpect(jsonPath("$.id").value("uuid-1"))
				.andExpect(jsonPath("$.ownerId").value("USER-123"))
				.andExpect(jsonPath("$.accountNumber").value("1234567890"))
				.andExpect(jsonPath("$.balance").value(250.00));

		verify(getAccountUseCase).getAccount(accountNumber, requesterId);
	}

	@Test
	void shouldReturn200AndAccountsWhenGettingAccounts() throws Exception {
		String requesterId = "USER-123";
		Account mockAccount = new Account("uuid-1", requesterId, "1234567890", new BigDecimal("250.00"), 1L);
		com.example.demo.common.application.port.in.dto.PageRequest pageRequest = new com.example.demo.common.application.port.in.dto.PageRequest(
				0, 10);
		com.example.demo.common.application.port.in.dto.PageResult<Account> mockPageResult = new com.example.demo.common.application.port.in.dto.PageResult<>(
				java.util.List.of(mockAccount), 0, 10, 1, 1);

		when(getAccountsUseCase.getAccountsByUserId(requesterId, pageRequest)).thenReturn(mockPageResult);

		mockMvc.perform(get("/api/accounts").param("page", "0").param("size", "10")
				.with(user(new CustomUserDetails(requesterId, "test@test.com", "pass", Collections.emptyList()))))
				.andExpect(status().isOk()).andExpect(jsonPath("$.content[0].id").value("uuid-1"))
				.andExpect(jsonPath("$.content[0].ownerId").value("USER-123"))
				.andExpect(jsonPath("$.content[0].accountNumber").value("1234567890"))
				.andExpect(jsonPath("$.content[0].balance").value(250.00))
				.andExpect(jsonPath("$.totalElements").value(1));

		verify(getAccountsUseCase).getAccountsByUserId(requesterId, pageRequest);
	}
}
