package com.example.demo.application.service;

import com.example.demo.application.port.in.dto.PageRequest;
import com.example.demo.application.port.in.dto.PageResult;
import com.example.demo.application.port.out.AccountRepository;
import com.example.demo.application.port.out.TransactionRecordRepository;
import com.example.demo.domain.model.Account;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionQueryServiceTest {

    @Mock
    private TransactionRecordRepository transactionRecordRepository;
    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private TransactionQueryService service;

    @Test
    void shouldRetrieveTransactionsWhenAuthorized() {
        Account account = new Account("USER-1", "ACC-123456");
        PageRequest pageRequest = new PageRequest(0, 10);

        when(accountRepository.findByAccountNumber("ACC-123456")).thenReturn(Optional.of(account));
        when(transactionRecordRepository.findByAccountNumber("ACC-123456", pageRequest))
                .thenReturn(new PageResult<>(Collections.emptyList(), 0, 10, 0, 0));

        service.getTransactions("ACC-123456", pageRequest, "USER-1");

        verify(transactionRecordRepository).findByAccountNumber("ACC-123456", pageRequest);
    }

    @Test
    void shouldBlockRetrievingTransactionsForUnauthorizedUser() {
        Account account = new Account("USER-1", "ACC-123456");
        PageRequest pageRequest = new PageRequest(0, 10);

        when(accountRepository.findByAccountNumber("ACC-123456")).thenReturn(Optional.of(account));

        assertThrows(SecurityException.class, () -> service.getTransactions("ACC-123456", pageRequest, "HACKER"));

        verify(transactionRecordRepository, never()).findByAccountNumber(anyString(), any());
    }
}