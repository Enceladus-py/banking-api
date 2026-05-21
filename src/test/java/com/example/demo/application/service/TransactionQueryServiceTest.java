package com.example.demo.application.service;

import com.example.demo.application.port.in.dto.PageRequest;
import com.example.demo.application.port.in.dto.PageResult;
import com.example.demo.application.port.out.TransactionRecordRepository;
import com.example.demo.domain.model.TransactionRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionQueryServiceTest {

    @Mock
    private TransactionRecordRepository transactionRecordRepository;

    @InjectMocks
    private TransactionQueryService transactionQueryService;

    @Test
    void shouldRetrievePaginatedTransactions() {
        // Arrange
        String accountNumber = "1234567890";
        PageRequest pageRequest = new PageRequest(1, 10);

        PageResult<TransactionRecord> expectedResult = new PageResult<>(
                Collections.emptyList(), 1, 10, 0, 0);

        when(transactionRecordRepository.findByAccountNumber(accountNumber, pageRequest))
                .thenReturn(expectedResult);

        // Act
        PageResult<TransactionRecord> actualResult = transactionQueryService.getTransactions(accountNumber,
                pageRequest);

        // Assert
        assertEquals(expectedResult, actualResult);
        verify(transactionRecordRepository, times(1)).findByAccountNumber(accountNumber, pageRequest);
    }
}