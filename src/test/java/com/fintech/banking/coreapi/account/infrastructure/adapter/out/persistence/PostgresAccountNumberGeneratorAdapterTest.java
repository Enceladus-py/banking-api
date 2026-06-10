package com.fintech.banking.coreapi.account.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import com.fintech.banking.coreapi.account.infrastructure.adapter.out.persistence.repository.SpringDataAccountRepository;
import com.fintech.banking.coreapi.account.infrastructure.adapter.out.persistence.repository.SpringDataAvailableAccountNumberRepository;

@ExtendWith(MockitoExtension.class)
class PostgresAccountNumberGeneratorAdapterTest {

	@Mock
	private SpringDataAvailableAccountNumberRepository poolRepository;

	@Mock
	private SpringDataAccountRepository accountRepository;

	@Mock
	private Environment environment;

	@Test
	void shouldDetectH2FromDriver() {
		when(environment.getProperty("spring.datasource.driver-class-name", "")).thenReturn("org.h2.Driver");
		when(environment.getProperty("spring.datasource.url", "")).thenReturn("jdbc:postgresql://localhost:5432/db");

		PostgresAccountNumberGeneratorAdapter adapter = new PostgresAccountNumberGeneratorAdapter(poolRepository,
				accountRepository, environment);

		when(poolRepository.getAvailableNumberWithLock()).thenReturn(Optional.of("1234567890"));

		String result = adapter.getNextAvailableNumber();

		assertEquals("1234567890", result);
		verify(poolRepository).deleteById("1234567890");
	}

	@Test
	void shouldDetectH2FromUrl() {
		when(environment.getProperty("spring.datasource.driver-class-name", "")).thenReturn("org.postgresql.Driver");
		when(environment.getProperty("spring.datasource.url", "")).thenReturn("jdbc:h2:mem:testdb");

		PostgresAccountNumberGeneratorAdapter adapter = new PostgresAccountNumberGeneratorAdapter(poolRepository,
				accountRepository, environment);

		when(poolRepository.getAvailableNumberWithLock()).thenReturn(Optional.of("1234567890"));

		String result = adapter.getNextAvailableNumber();

		assertEquals("1234567890", result);
		verify(poolRepository).deleteById("1234567890");
	}

	@Test
	void shouldGenerateFromH2WhenPoolEmpty() {
		when(environment.getProperty("spring.datasource.driver-class-name", "")).thenReturn("org.h2.Driver");
		when(environment.getProperty("spring.datasource.url", "")).thenReturn("");

		PostgresAccountNumberGeneratorAdapter adapter = new PostgresAccountNumberGeneratorAdapter(poolRepository,
				accountRepository, environment);

		when(poolRepository.getAvailableNumberWithLock()).thenReturn(Optional.empty());
		when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
		when(poolRepository.existsById(anyString())).thenReturn(false);

		String result = adapter.getNextAvailableNumber();

		assertNotNull(result);
		assertEquals(10, result.length());
	}

	@Test
	void shouldGetFromPostgresPool() {
		when(environment.getProperty("spring.datasource.driver-class-name", "")).thenReturn("org.postgresql.Driver");
		when(environment.getProperty("spring.datasource.url", "")).thenReturn("jdbc:postgresql://localhost:5432/db");

		PostgresAccountNumberGeneratorAdapter adapter = new PostgresAccountNumberGeneratorAdapter(poolRepository,
				accountRepository, environment);

		when(poolRepository.popAvailableNumberPostgres()).thenReturn(Optional.of("1234567890"));

		String result = adapter.getNextAvailableNumber();

		assertEquals("1234567890", result);
	}

	@Test
	void shouldGenerateFromPostgresWhenPoolEmptyAndRetryOnCollision() {
		when(environment.getProperty("spring.datasource.driver-class-name", "")).thenReturn("org.postgresql.Driver");
		when(environment.getProperty("spring.datasource.url", "")).thenReturn("jdbc:postgresql://localhost:5432/db");

		PostgresAccountNumberGeneratorAdapter adapter = new PostgresAccountNumberGeneratorAdapter(poolRepository,
				accountRepository, environment);

		when(poolRepository.popAvailableNumberPostgres()).thenReturn(Optional.empty());

		// First try: collides in account repo (existsById not called)
		// Second try: doesn't collide in account repo, but collides in pool repo
		// Third try: success (neither collides)
		when(accountRepository.existsByAccountNumber(anyString())).thenReturn(true, false, false);
		when(poolRepository.existsById(anyString())).thenReturn(true, false);

		String result = adapter.getNextAvailableNumber();

		assertNotNull(result);
		assertEquals(10, result.length());
		verify(accountRepository, times(3)).existsByAccountNumber(anyString());
		verify(poolRepository, times(2)).existsById(anyString());
	}
}
