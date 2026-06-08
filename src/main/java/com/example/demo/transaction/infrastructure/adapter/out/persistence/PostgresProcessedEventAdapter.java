package com.example.demo.transaction.infrastructure.adapter.out.persistence;

import java.time.Instant;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.transaction.application.port.out.ProcessedEventPort;
import com.example.demo.transaction.infrastructure.adapter.out.persistence.entity.ProcessedEventJpaEntity;
import com.example.demo.transaction.infrastructure.adapter.out.persistence.repository.SpringDataProcessedEventRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Persistence adapter for managing processed events for idempotency.
 */
@Component
@Slf4j
public class PostgresProcessedEventAdapter implements ProcessedEventPort {

	private final SpringDataProcessedEventRepository repository;

	/**
	 * Constructs the adapter with the specified repository.
	 *
	 * @param repository
	 *            the Spring Data repository
	 */
	public PostgresProcessedEventAdapter(SpringDataProcessedEventRepository repository) {
		this.repository = repository;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean saveIfAbsent(UUID eventId) {
		try {
			repository.saveAndFlush(new ProcessedEventJpaEntity(eventId, Instant.now()));
			return true;
		} catch (DataIntegrityViolationException e) {
			log.warn("Event {} has already been processed. Ignoring duplicate.", eventId);
			return false;
		}
	}
}
