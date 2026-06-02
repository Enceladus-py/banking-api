package com.example.demo.infrastructure.adapter.in.web;

import com.example.demo.application.port.in.GetAccountTransactionsUseCase;
import com.example.demo.application.port.in.GetTransactionUseCase;
import com.example.demo.application.port.in.dto.PageRequest;
import com.example.demo.application.port.in.dto.PageResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetAccountTransactionsUseCase getAccountTransactionsUseCase;

    @MockitoBean
    private GetTransactionUseCase getTransactionUseCase;

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
                .header("X-User-Id", requesterId) // Added the authentication header
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(2))
                .andExpect(jsonPath("$.pageSize").value(5))
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
                .header("X-User-Id", requesterId) // Added the authentication header
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify the Controller used the default values defined in @RequestParam AND
        // extracted the user ID
        verify(getAccountTransactionsUseCase).getTransactions(accountNumber, new PageRequest(0, 10), requesterId);
    }

    @Test
    void shouldReturn200AndTransactionResponseWhenGettingById() throws Exception {
        String txId = "tx-12345";
        String requesterId = "USER-123";
        com.example.demo.domain.model.TransactionRecord tx = new com.example.demo.domain.model.TransactionRecord(txId, "ACC-SRC", "ACC-TGT", new java.math.BigDecimal("50.00"), com.example.demo.domain.model.TransactionRecord.TransactionType.TRANSFER, java.time.LocalDateTime.now(), com.example.demo.domain.model.TransactionRecord.TransactionStatus.COMPLETED, null);

        when(getTransactionUseCase.getTransaction(eq(txId), eq(requesterId))).thenReturn(tx);

        mockMvc.perform(get("/api/transactions/id/{transactionId}", txId)
                .header("X-User-Id", requesterId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(txId))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.amount").value(50.00));

        verify(getTransactionUseCase).getTransaction(txId, requesterId);
    }
}