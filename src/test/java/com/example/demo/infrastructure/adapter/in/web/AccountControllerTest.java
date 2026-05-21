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
import static org.junit.jupiter.api.Assertions.*;

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
    void shouldReturn201WhenAccountIsCreated() throws Exception {
        // 1. Arrange: Prepare the mock response from the Domain
        Account mockDomainAccount = new Account("uuid-1", "Berat", "Dalsuna", "ACC1234567", BigDecimal.ZERO);
        when(createAccountUseCase.createAccount(any(CreateAccountCommand.class))).thenReturn(mockDomainAccount);

        // Prepare the JSON body we will send
        String jsonPayload = """
                {
                    "name": "Berat",
                    "surname": "Dalsuna"
                }
                """;

        // 2 & 3. Act & Assert: Send the request and verify the response
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isCreated()) // Expect HTTP 201
                .andExpect(jsonPath("$.id").value("uuid-1"))
                .andExpect(jsonPath("$.name").value("Berat"))
                .andExpect(jsonPath("$.surname").value("Dalsuna"))
                .andExpect(jsonPath("$.accountNumber").value("ACC1234567"))
                .andExpect(jsonPath("$.balance").value(0.00));
    }

    @Test
    void shouldReturn400BadRequestWhenNameIsBlank() throws Exception {
        // Arrange: Provide an empty name
        String jsonPayload = """
                {
                    "name": "",
                    "surname": "Dalsuna"
                }
                """;

        // Act & Assert: We do not mock the Use Case here because
        // the request should be blocked before it ever reaches the Use Case!
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isBadRequest()); // Expect HTTP 400
    }

    @Test
    void shouldTrimWhitespaceBeforeCallingUseCase() throws Exception {
        // Arrange: Provide strings with extra spaces
        Account mockDomainAccount = new Account("uuid-1", "Berat", "Dalsuna", "ACC1234567", BigDecimal.ZERO);
        when(createAccountUseCase.createAccount(any(CreateAccountCommand.class))).thenReturn(mockDomainAccount);

        String jsonPayload = """
                {
                    "name": "  Berat  ",
                    "surname": " Dalsuna "
                }
                """;

        // Act
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isCreated());

        // Assert: Verify the Command passed to the Use Case was completely trimmed
        org.mockito.ArgumentCaptor<CreateAccountCommand> commandCaptor = org.mockito.ArgumentCaptor
                .forClass(CreateAccountCommand.class);

        org.mockito.Mockito.verify(createAccountUseCase).createAccount(commandCaptor.capture());

        CreateAccountCommand capturedCommand = commandCaptor.getValue();
        assertEquals("Berat", capturedCommand.name());
        assertEquals("Dalsuna", capturedCommand.surname());
    }

    @Test
    void shouldReturn200WhenDepositIsSuccessful() throws Exception {
        // Arrange
        String accountNumber = "A1B2C3D4E5";
        Account updatedAccount = new Account("uuid-123", "Berat", "Dalsuna", accountNumber, new BigDecimal("100.00"));

        when(depositMoneyUseCase.deposit(any(DepositCommand.class))).thenReturn(updatedAccount);

        String jsonPayload = """
                {
                    "accountNumber": "A1B2C3D4E5",
                    "amount": 100.00
                }
                """;

        // Act & Assert
        mockMvc.perform(put("/api/accounts/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(accountNumber))
                .andExpect(jsonPath("$.balance").value(100.00));

        verify(depositMoneyUseCase, times(1)).deposit(any(DepositCommand.class));
    }

    @Test
    void shouldReturn400BadRequestWhenAmountIsNegative() throws Exception {
        // Arrange
        String jsonPayload = """
                {
                    "accountNumber": "A1B2C3D4E5",
                    "amount": -25.50
                }
                """;

        // Act & Assert
        mockMvc.perform(put("/api/accounts/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isBadRequest()); // Blocked by Web DTO Validation (@Positive)

        // Ensure the application core was never invoked
        verify(depositMoneyUseCase, never()).deposit(any(DepositCommand.class));
    }

    @Test
    void shouldReturn400BadRequestWhenAccountNumberIsInvalidLength() throws Exception {
        // Arrange
        String jsonPayload = """
                {
                    "accountNumber": "ABC",
                    "amount": 50.00
                }
                """;

        // Act & Assert
        mockMvc.perform(put("/api/accounts/deposit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isBadRequest()); // Blocked by Web DTO Validation (@Size)

        verify(depositMoneyUseCase, never()).deposit(any(DepositCommand.class));
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
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isNotFound()); // We expect an HTTP 404
    }

    @Test
    void shouldReturn200AndUpdatedAccountOnSuccessfulWithdrawal() throws Exception {
        // Arrange
        String accountNumber = "1122334455";
        Account expectedAccount = new Account("uuid-999", "Berat", "Dalsuna", accountNumber, new BigDecimal("350.00"));

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
                .thenThrow(new IllegalStateException("Insufficient funds for withdrawal"));

        String overDraftPayload = """
                {
                    "accountNumber": "1122334455",
                    "amount": 9000.00
                }
                """;

        // Act & Assert: GlobalExceptionHandler should map this to HTTP 400
        mockMvc.perform(put("/api/accounts/withdraw")
                .contentType(MediaType.APPLICATION_JSON)
                .content(overDraftPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient funds for withdrawal"));
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
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
                .andExpect(status().isBadRequest());

        // Ensure the core business logic was never touched
        verify(withdrawMoneyUseCase, never()).withdraw(any(WithdrawMoneyUseCase.WithdrawCommand.class));
    }
}