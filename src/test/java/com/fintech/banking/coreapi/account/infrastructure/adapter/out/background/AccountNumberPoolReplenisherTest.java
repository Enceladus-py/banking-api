package com.fintech.banking.coreapi.account.infrastructure.adapter.out.background;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fintech.banking.coreapi.account.infrastructure.adapter.out.persistence.repository.SpringDataAccountRepository;
import com.fintech.banking.coreapi.account.infrastructure.adapter.out.persistence.repository.SpringDataAvailableAccountNumberRepository;

@ExtendWith(MockitoExtension.class)
class AccountNumberPoolReplenisherTest {

	@Mock
	private SpringDataAvailableAccountNumberRepository poolRepository;

	@Mock
	private SpringDataAccountRepository accountRepository;

	@InjectMocks
	private AccountNumberPoolReplenisher replenisher;

	@Test
	void shouldNotReplenishWhenPoolIsAboveThreshold() {
		when(poolRepository.count()).thenReturn(600L);

		replenisher.replenishPool();

		verify(poolRepository, never()).saveAll(any());
	}

	@Test
	void shouldReplenishWhenPoolIsBelowThreshold() {
		when(poolRepository.count()).thenReturn(400L); // threshold is 500
		// We need to generate 1000 - 400 = 600 numbers.

		when(accountRepository.findExistingAccountNumbers(anySet())).thenReturn(java.util.Collections.emptySet());
		when(poolRepository.findExistingAccountNumbers(anySet())).thenReturn(java.util.Collections.emptySet());

		replenisher.replenishPool();

		verify(poolRepository).saveAll(any());
	}

	@Test
	void shouldRetryWhenCollisionsOccur() {
		when(poolRepository.count()).thenReturn(499L); // Generate 501

		// First iteration returns all 501 as collisions.
		// Second iteration returns no collisions.
		when(accountRepository.findExistingAccountNumbers(anySet())).thenAnswer(invocation -> {
			java.util.Set<String> args = invocation.getArgument(0);
			if (args.size() == 501) {
				return new java.util.HashSet<>(args); // all collide
			}
			return java.util.Collections.emptySet();
		});

		when(poolRepository.findExistingAccountNumbers(anySet())).thenReturn(java.util.Collections.emptySet());

		replenisher.replenishPool();

		verify(poolRepository).saveAll(any());
	}
}
