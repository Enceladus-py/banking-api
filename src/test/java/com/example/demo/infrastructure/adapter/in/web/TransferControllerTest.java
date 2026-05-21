package com.example.demo.infrastructure.adapter.in.web;

import com.example.demo.application.port.in.TransferMoneyUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransferController.class)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TransferMoneyUseCase transferMoneyUseCase;

    @Test
    void shouldReturn200OnSuccessfulTransfer() throws Exception {
        String validPayload = """
                {
                    "sourceAccountNumber": "1111111111",
                    "targetAccountNumber": "2222222222",
                    "amount": 100.50
                }
                """;

        // Since it's a void method, we just let it execute without throwing
        doNothing().when(transferMoneyUseCase).transfer(any(TransferMoneyUseCase.TransferCommand.class));

        mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validPayload))
                .andExpect(status().isOk());
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

        doThrow(new IllegalStateException("Insufficient funds"))
                .when(transferMoneyUseCase).transfer(any(TransferMoneyUseCase.TransferCommand.class));

        mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(overdraftPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Insufficient funds"));
    }

    @Test
    void shouldReturn400WhenPayloadViolatesValidation() throws Exception {
        // Negative amount violates @Positive
        String invalidPayload = """
                {
                    "sourceAccountNumber": "1111111111",
                    "targetAccountNumber": "2222222222",
                    "amount": -50.00
                }
                """;

        mockMvc.perform(post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidPayload))
                .andExpect(status().isBadRequest());

        // The core business logic should be protected from bad data
        verify(transferMoneyUseCase, never()).transfer(any(TransferMoneyUseCase.TransferCommand.class));
    }
}