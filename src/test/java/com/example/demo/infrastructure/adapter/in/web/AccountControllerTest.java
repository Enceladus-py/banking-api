package com.example.demo.infrastructure.adapter.in.web;

import com.example.demo.application.port.in.CreateAccountUseCase;
import com.example.demo.application.port.in.CreateAccountUseCase.CreateAccountCommand;
import com.example.demo.domain.model.Account;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc; // Simulates HTTP requests

    @MockitoBean
    private CreateAccountUseCase createAccountUseCase; // Fakes the inner hexagon

    @Test
    void shouldReturn201WhenAccountIsCreated() throws Exception {
        // 1. Arrange: Prepare the mock response from the Domain
        Account mockDomainAccount = new Account("uuid-1", "Berat", "Dalsuna", "ACC123", BigDecimal.ZERO);
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
                .andExpect(jsonPath("$.accountNumber").value("ACC123"))
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
}