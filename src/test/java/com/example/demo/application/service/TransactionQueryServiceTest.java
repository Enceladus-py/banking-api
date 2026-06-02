package com.example.demo.application.service;

import com.example.demo.application.port.in.dto.PageRequest;
import com.example.demo.application.port.in.dto.PageResult;
import com.example.demo.application.port.out.AccountRepository;
import com.example.demo.application.port.out.TransactionRecordRepository;
import com.example.demo.domain.model.Account;
import com.example.demo.domain.model.TransactionRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
        Account account = Account.createNew("USER-1", "ACC-123456");
        PageRequest pageRequest = new PageRequest(0, 10);

        when(accountRepository.findByAccountNumber("ACC-123456")).thenReturn(Optional.of(account));
        when(transactionRecordRepository.findByAccountNumber("ACC-123456", pageRequest))
                .thenReturn(new PageResult<>(Collections.emptyList(), 0, 10, 0, 0));

        service.getTransactions("ACC-123456", pageRequest, "USER-1");

        verify(transactionRecordRepository).findByAccountNumber("ACC-123456", pageRequest);
    }

    @Test
    void shouldBlockRetrievingTransactionsForUnauthorizedUser() {
        Account account = Account.createNew("USER-1", "ACC-123456");
        PageRequest pageRequest = new PageRequest(0, 10);

        when(accountRepository.findByAccountNumber("ACC-123456")).thenReturn(Optional.of(account));

        assertThrows(SecurityException.class, () -> service.getTransactions("ACC-123456", pageRequest, "HACKER"));

        verify(transactionRecordRepository, never()).findByAccountNumber(anyString(), any());
    }

    @Test
    void shouldRetrieveTransactionByIdWhenAuthorizedAsSource() {
        TransactionRecord tx = new TransactionRecord("tx-123", "1234567890", "0987654321", new BigDecimal("100"), TransactionRecord.TransactionType.TRANSFER, java.time.LocalDateTime.now(), TransactionRecord.TransactionStatus.PENDING, null);
        Account sourceAccount = Account.createNew("USER-1", "1234567890");
        
        when(transactionRecordRepository.findById("tx-123")).thenReturn(Optional.of(tx));
        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.of(sourceAccount));

        TransactionRecord result = service.getTransaction("tx-123", "USER-1");

        org.junit.jupiter.api.Assertions.assertEquals("tx-123", result.getId());
    }

    @Test
    void shouldRetrieveTransactionByIdWhenAuthorizedAsTarget() {
        TransactionRecord tx = new TransactionRecord("tx-123", "1234567890", "0987654321", new BigDecimal("100"), TransactionRecord.TransactionType.TRANSFER, java.time.LocalDateTime.now(), TransactionRecord.TransactionStatus.PENDING, null);
        Account targetAccount = Account.createNew("USER-2", "0987654321");
        
        when(transactionRecordRepository.findById("tx-123")).thenReturn(Optional.of(tx));
        lenient().when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumber("0987654321")).thenReturn(Optional.of(targetAccount));

        TransactionRecord result = service.getTransaction("tx-123", "USER-2");

        org.junit.jupiter.api.Assertions.assertEquals("tx-123", result.getId());
    }

    @Test
    void shouldBlockRetrievingTransactionByIdForUnauthorizedUser() {
        TransactionRecord tx = new TransactionRecord("tx-123", "1234567890", "0987654321", new BigDecimal("100"), TransactionRecord.TransactionType.TRANSFER, java.time.LocalDateTime.now(), TransactionRecord.TransactionStatus.PENDING, null);
        
        when(transactionRecordRepository.findById("tx-123")).thenReturn(Optional.of(tx));
        lenient().when(accountRepository.findByAccountNumber("1234567890")).thenReturn(Optional.empty());
        lenient().when(accountRepository.findByAccountNumber("0987654321")).thenReturn(Optional.empty());

        assertThrows(SecurityException.class, () -> service.getTransaction("tx-123", "HACKER"));
    }
}