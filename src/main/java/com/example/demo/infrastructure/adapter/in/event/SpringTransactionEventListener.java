package com.example.demo.infrastructure.adapter.in.event;

import java.time.LocalDateTime;

import org.springframework.context.event.EventListener;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.application.port.in.ProcessTransactionUseCase;
import com.example.demo.domain.event.TransactionPendingEvent;
import com.example.demo.infrastructure.adapter.out.persistence.entity.ProcessedEventJpaEntity;
import com.example.demo.infrastructure.adapter.out.persistence.repository.SpringDataProcessedEventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

		// Idempotency: attempt to insert a processed_events row first.
		// If the row already exists, the UNIQUE constraint on 'id' raises a
		// DataIntegrityViolationException — we catch it and skip processing.
		// This is atomic (no TOCTOU race between check and insert).
		try {
			processedEventRepository.saveAndFlush(new ProcessedEventJpaEntity(event.eventId(), LocalDateTime.now()));
		} catch (DataIntegrityViolationException e) {
			log.warn("Event {} has already been processed. Ignoring duplicate.", event.eventId());
			return;
		}

		processTransactionUseCase.process(event);
	}
}
