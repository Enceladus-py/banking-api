package com.fintech.banking.coreapi.account.infrastructure.adapter.out.background;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fintech.banking.coreapi.account.domain.util.AccountNumberGenerator;
import com.fintech.banking.coreapi.account.infrastructure.adapter.out.persistence.entity.AvailableAccountNumberJpaEntity;
import com.fintech.banking.coreapi.account.infrastructure.adapter.out.persistence.repository.SpringDataAccountRepository;
import com.fintech.banking.coreapi.account.infrastructure.adapter.out.persistence.repository.SpringDataAvailableAccountNumberRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * Background job that ensures the account number pool is adequately stocked.
 */
@Component
@Slf4j
public class AccountNumberPoolReplenisher {

	private final SpringDataAvailableAccountNumberRepository poolRepository;
	private final SpringDataAccountRepository accountRepository;

	/**
	 * Constructs a new AccountNumberPoolReplenisher.
	 *
	 * @param poolRepository
	 *            the repository for available account numbers
	 * @param accountRepository
	 *            the repository for accounts
	 */
	public AccountNumberPoolReplenisher(SpringDataAvailableAccountNumberRepository poolRepository,
			SpringDataAccountRepository accountRepository) {
		this.poolRepository = poolRepository;
		this.accountRepository = accountRepository;
	}

	private static final int POOL_THRESHOLD = 500;
	private static final int BATCH_SIZE = 1000;

	/**
	 * Periodically checks the pool size and replenishes if necessary.
	 */
	@Scheduled(fixedDelay = 60000)
	@Transactional
	public void replenishPool() {
		long currentCount = poolRepository.count();
		if (currentCount < POOL_THRESHOLD) {
			log.info("Account number pool size ({}) is below threshold ({}). Replenishing...", currentCount,
					POOL_THRESHOLD);

			int numbersToGenerate = BATCH_SIZE - (int) currentCount;
			java.util.Set<String> newNumbers = new java.util.HashSet<>();

			while (newNumbers.size() < numbersToGenerate) {
				java.util.Set<String> candidates = new java.util.HashSet<>();
				int remaining = numbersToGenerate - newNumbers.size();
				for (int i = 0; i < remaining; i++) {
					candidates.add(AccountNumberGenerator.generateRandom10DigitNumber());
				}

				java.util.Set<String> existingInAccounts = accountRepository.findExistingAccountNumbers(candidates);
				java.util.Set<String> existingInPool = poolRepository.findExistingAccountNumbers(candidates);

				candidates.removeAll(existingInAccounts);
				candidates.removeAll(existingInPool);

				newNumbers.addAll(candidates);
			}

			List<AvailableAccountNumberJpaEntity> newEntities = newNumbers.stream()
					.map(AvailableAccountNumberJpaEntity::new).toList();

			poolRepository.saveAll(newEntities);
			log.info("Added {} new account numbers to the pool.", newEntities.size());
		}
	}
}
