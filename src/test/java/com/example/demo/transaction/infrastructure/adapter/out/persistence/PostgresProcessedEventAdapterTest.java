package com.example.demo.transaction.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.example.demo.transaction.infrastructure.adapter.out.persistence.entity.ProcessedEventJpaEntity;
import com.example.demo.transaction.infrastructure.adapter.out.persistence.repository.SpringDataProcessedEventRepository;

@ExtendWith(MockitoExtension.class)
class PostgresProcessedEventAdapterTest {

	@Mock
	private SpringDataProcessedEventRepository repository;

	@InjectMocks
	private PostgresProcessedEventAdapter adapter;

	@Test
	void shouldReturnTrueWhenEventIsSuccessfullySaved() {
		UUID eventId = UUID.randomUUID();

		when(repository.saveAndFlush(any(ProcessedEventJpaEntity.class)))
				.thenReturn(new ProcessedEventJpaEntity(eventId, java.time.Instant.now()));

		boolean result = adapter.saveIfAbsent(eventId);

		assertTrue(result, "Expected saveIfAbsent to return true when saving succeeds");
	}

	@Test
	void shouldReturnFalseWhenDataIntegrityViolationExceptionIsThrown() {
		UUID eventId = UUID.randomUUID();

		when(repository.saveAndFlush(any(ProcessedEventJpaEntity.class)))
				.thenThrow(new DataIntegrityViolationException("Duplicate key violation"));

		boolean result = adapter.saveIfAbsent(eventId);

		assertFalse(result, "Expected saveIfAbsent to return false when duplicate exception is thrown");
	}
}
