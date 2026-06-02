package com.example.demo.infrastructure.adapter.in.event;

import com.example.demo.application.port.in.ProcessTransactionUseCase;
import com.example.demo.domain.event.TransactionPendingEvent;
import com.example.demo.infrastructure.adapter.out.persistence.entity.ProcessedEventJpaEntity;
import com.example.demo.infrastructure.adapter.out.persistence.repository.SpringDataProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class SpringTransactionEventListener {

    private final ProcessTransactionUseCase processTransactionUseCase;
    private final SpringDataProcessedEventRepository processedEventRepository;

    @EventListener
    @Transactional
    public void onTransactionPending(TransactionPendingEvent event) {
        log.info("Received transaction pending event: {} for transaction: {}", event.eventId(), event.transactionId());

        // 1. Deduplication (Idempotency) check
        if (processedEventRepository.existsById(event.eventId())) {
            log.warn("Event {} has already been processed. Ignoring.", event.eventId());
            return;
        }

        // 2. Mark event as processed
        processedEventRepository.save(new ProcessedEventJpaEntity(event.eventId(), LocalDateTime.now()));

        // 3. Process transaction
        processTransactionUseCase.process(event);
    }
}
