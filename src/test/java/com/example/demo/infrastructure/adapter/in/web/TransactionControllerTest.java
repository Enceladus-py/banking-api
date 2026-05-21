package com.example.demo.infrastructure.adapter.in.web;

import com.example.demo.application.port.in.GetAccountTransactionsUseCase;
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

    @Test
    void shouldReturnPaginatedTransactionsWithCustomParams() throws Exception {
        // Arrange
        String accountNumber = "1234567890";
        PageResult<?> emptyResult = new PageResult<>(Collections.emptyList(), 2, 5, 10, 2);

        when(getAccountTransactionsUseCase.getTransactions(eq(accountNumber), any(PageRequest.class)))
                .thenAnswer(invocation -> emptyResult);

        // Act & Assert
        mockMvc.perform(get("/api/transactions/{accountNumber}?page=2&size=5", accountNumber)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pageNumber").value(2))
                .andExpect(jsonPath("$.pageSize").value(5))
                .andExpect(jsonPath("$.totalElements").value(10));

        // Verify the Controller passed the exact HTTP params to the Use Case
        verify(getAccountTransactionsUseCase).getTransactions(accountNumber, new PageRequest(2, 5));
    }

    @Test
    void shouldReturnPaginatedTransactionsWithDefaultParams() throws Exception {
        // Arrange
        String accountNumber = "1234567890";
        PageResult<?> defaultResult = new PageResult<>(Collections.emptyList(), 0, 10, 0, 0);

        when(getAccountTransactionsUseCase.getTransactions(eq(accountNumber), any(PageRequest.class)))
                .thenAnswer(invocation -> defaultResult);

        // Act & Assert
        // We omit ?page= and ?size=
        mockMvc.perform(get("/api/transactions/{accountNumber}", accountNumber)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify the Controller used the default values defined in @RequestParam
        verify(getAccountTransactionsUseCase).getTransactions(accountNumber, new PageRequest(0, 10));
    }
}