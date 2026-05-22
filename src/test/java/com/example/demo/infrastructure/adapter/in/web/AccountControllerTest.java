package com.example.demo.infrastructure.adapter.in.web;

import com.example.demo.application.port.in.CreateAccountUseCase;
import com.example.demo.application.port.in.CreateAccountUseCase.CreateAccountCommand;
import com.example.demo.application.port.in.DepositMoneyUseCase;
import com.example.demo.application.port.in.WithdrawMoneyUseCase;
import com.example.demo.application.port.in.DepositMoneyUseCase.DepositCommand;
import com.example.demo.domain.model.Account;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc; // Simulates HTTP requests

    @MockitoBean
    private CreateAccountUseCase createAccountUseCase; // Fakes the inner hexagon

    @MockitoBean
    private DepositMoneyUseCase depositMoneyUseCase;

    @MockitoBean
    private WithdrawMoneyUseCase withdrawMoneyUseCase;

    @Test
    void shouldReturn200WhenAccountIsCreated() throws Exception {
        // 1. Arrange: Prepare the mock response from the Domain
        Account mockDomainAccount = new Account("uuid-1", "USER-123", "ACC1234567", BigDecimal.ZERO);
        when(createAccountUseCase.createAccount(any(CreateAccountCommand.class))).thenReturn(mockDomainAccount);

        // 2 & 3. Act & Assert: Send request with ONLY the User ID header (no JSON body
        // needed anymore)
        mockMvc.perform(post("/api/accounts")
                .header("X-User-Id", "USER-123"))
                .andExpect(status().isOk()) // HTTP 200 OK
                .andExpect(jsonPath("$.id").value("uuid-1"))
                .andExpect(jsonPath("$.ownerId").value("USER-123"))
                .andExpect(jsonPath("$.accountNumber").value("ACC1234567"))
                .andExpect(jsonPath("$.balance").value(0.00));
    }

    @Test
    void shouldReturn400BadRequestWhenUserIdHeaderIsMissing() throws Exception {
        // Act & Assert: Attempting to call the endpoint without the authentication
        // header
        mockMvc.perform(post("/api/accounts"))
                .andExpect(status().isBadRequest()); // Spring automatically blocks requests missing
                                                     // required headers

        // Ensure use case is never called
        verify(createAccountUseCase, never()).createAccount(any());
    }

    @Test
    void shouldReturn200WhenDepositIsSuccessful() throws Exception {
        // Arrange
        String accountNumber = "A1B2C3D4E5";
        String requesterId = "USER-123";
        Account updatedAccount = new Account("uuid-123", requesterId, accountNumber, new BigDecimal("100.00"));

        when(depositMoneyUseCase.deposit(any(DepositCommand.class))).thenReturn(updatedAccount);

        String jsonPayload = """
                {
                    "accountNumber": "A1B2C3D4E5",
                    "amount": 100.00
                }
                """;

        // Act & Assert
        mockMvc.perform(put("/api/accounts/deposit")
                .header("X-User-Id", requesterId) // Added Security Header
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(accountNumber))
                .andExpect(jsonPath("$.balance").value(100.00));

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
        mockMvc.perform(put("/api/accounts/deposit")
                .header("X-User-Id", "USER-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isBadRequest()); // Blocked by Web DTO Validation (@Positive)

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
        mockMvc.perform(put("/api/accounts/deposit")
                .header("X-User-Id", "USER-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isBadRequest()); // Blocked by Web DTO Validation (@NotBlank)

        verify(depositMoneyUseCase, never()).deposit(any(DepositCommand.class));
    }

    @Test
    void shouldReturn403ForbiddenWhenUserAttemptsToTouchAnotherUsersAccount() throws Exception {
        // Arrange: Simulate the domain rejecting the action based on ownership
        when(depositMoneyUseCase.deposit(any(DepositCommand.class)))
                .thenThrow(new SecurityException(
                        "You are not authorized to deposit into this account"));

        String jsonPayload = """
                {
                    "accountNumber": "A1B2C3D4E5",
                    "amount": 100.00
                }
                """;

        // Act & Assert
        mockMvc.perform(put("/api/accounts/deposit")
                .header("X-User-Id", "HACKER-ID")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isForbidden()) // Ensure GlobalExceptionHandler maps
                                                   // SecurityException to 403
                .andExpect(jsonPath("$.message")
                        .value("You are not authorized to deposit into this account"));
    }

    @Test
    void shouldReturn404NotFoundWhenAccountNumberDoesNotExist() throws Exception {
        // Arrange: Tell the use case to throw an exception when called
        when(depositMoneyUseCase.deposit(any(DepositCommand.class)))
                .thenThrow(new IllegalArgumentException("Account not found"));

        String jsonPayload = """
                {
                    "accountNumber": "0000000000",
                    "amount": 100.00
                }
                """;

        // Act & Assert
        mockMvc.perform(put("/api/accounts/deposit")
                .header("X-User-Id", "USER-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isNotFound()); // We expect an HTTP 404
    }

    @Test
    void shouldReturn200AndUpdatedAccountOnSuccessfulWithdrawal() throws Exception {
        // Arrange
        String accountNumber = "1122334455";
        String requesterId = "USER-123";
        Account expectedAccount = new Account("uuid-999", requesterId, accountNumber, new BigDecimal("350.00"));

        when(withdrawMoneyUseCase.withdraw(any(WithdrawMoneyUseCase.WithdrawCommand.class)))
                .thenReturn(expectedAccount);

        String validPayload = """
                {
                    "accountNumber": "1122334455",
                    "amount": 150.00
                }
                """;

        // Act & Assert
        mockMvc.perform(put("/api/accounts/withdraw")
                .header("X-User-Id", requesterId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(accountNumber))
                .andExpect(jsonPath("$.balance").value(350.00));
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
        mockMvc.perform(put("/api/accounts/withdraw")
                .header("X-User-Id", "USER-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(overDraftPayload))
                .andExpect(status().isBadRequest())
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
        mockMvc.perform(put("/api/accounts/withdraw")
                .header("X-User-Id", "USER-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
                .andExpect(status().isBadRequest());

        // Ensure the core business logic was never touched
        verify(withdrawMoneyUseCase, never()).withdraw(any(WithdrawMoneyUseCase.WithdrawCommand.class));
    }
}