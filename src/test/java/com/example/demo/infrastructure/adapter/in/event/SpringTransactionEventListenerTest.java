package com.example.demo.infrastructure.adapter.in.event;

import com.example.demo.application.port.in.ProcessTransactionUseCase;
import com.example.demo.domain.event.TransactionPendingEvent;
import com.example.demo.domain.model.TransactionRecord;
import com.example.demo.infrastructure.adapter.out.persistence.entity.ProcessedEventJpaEntity;
import com.example.demo.infrastructure.adapter.out.persistence.repository.SpringDataProcessedEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpringTransactionEventListenerTest {

    @Mock
    private ProcessTransactionUseCase processTransactionUseCase;

    @Mock
    private SpringDataProcessedEventRepository processedEventRepository;

    @InjectMocks
    private SpringTransactionEventListener listener;

    @Test
    void shouldProcessEventSuccessfully() {
        UUID eventId = UUID.randomUUID();
        TransactionPendingEvent event = new TransactionPendingEvent(
                eventId,
                UUID.randomUUID().toString(),
                Instant.now(),
                "SRC123",
                "TGT456",
                new BigDecimal("100.00"),
                TransactionRecord.TransactionType.TRANSFER,
                "user1"
        );

        // When saving the processed event succeeds
        when(processedEventRepository.saveAndFlush(any(ProcessedEventJpaEntity.class)))
                .thenReturn(new ProcessedEventJpaEntity(eventId, java.time.LocalDateTime.now()));

        listener.onTransactionPending(event);

        verify(processedEventRepository, times(1)).saveAndFlush(any(ProcessedEventJpaEntity.class));
        verify(processTransactionUseCase, times(1)).process(event);
    }

    @Test
    void shouldIgnoreDuplicateEventWhenDataIntegrityViolationExceptionIsThrown() {
        UUID eventId = UUID.randomUUID();
        TransactionPendingEvent event = new TransactionPendingEvent(
                eventId,
                UUID.randomUUID().toString(),
                Instant.now(),
                "SRC123",
                "TGT456",
                new BigDecimal("100.00"),
                TransactionRecord.TransactionType.TRANSFER,
                "user1"
        );

        // When saving throws DataIntegrityViolationException (duplicate event)
        when(processedEventRepository.saveAndFlush(any(ProcessedEventJpaEntity.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate key violation"));

        listener.onTransactionPending(event);

        verify(processedEventRepository, times(1)).saveAndFlush(any(ProcessedEventJpaEntity.class));
        // processTransactionUseCase.process should never be called
        verify(processTransactionUseCase, never()).process(any());
    }
}
