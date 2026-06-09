package com.example.demo.transaction.infrastructure.adapter.in.web;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.account.infrastructure.adapter.out.persistence.repository.SpringDataAccountRepository;
import com.example.demo.transaction.domain.event.EventType;
import com.example.demo.transaction.infrastructure.adapter.out.event.OutboxEventScheduler;
import com.example.demo.transaction.infrastructure.adapter.out.persistence.entity.OutboxEventJpaEntity;
import com.example.demo.transaction.infrastructure.adapter.out.persistence.entity.OutboxStatus;
import com.example.demo.transaction.infrastructure.adapter.out.persistence.entity.ProcessedEventJpaEntity;
import com.example.demo.transaction.infrastructure.adapter.out.persistence.repository.SpringDataOutboxEventRepository;
import com.example.demo.transaction.infrastructure.adapter.out.persistence.repository.SpringDataProcessedEventRepository;
import com.example.demo.transaction.infrastructure.adapter.out.persistence.repository.SpringDataTransactionRepository;
import com.example.demo.user.infrastructure.adapter.out.persistence.repository.SpringDataUserRepository;

@SpringBootTest(properties = {"outbox.scheduler.delay=9999999"})
@AutoConfigureMockMvc
@org.springframework.kafka.test.context.EmbeddedKafka(partitions = 1)
@org.springframework.test.context.TestPropertySource(properties = {
		"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
		"spring.kafka.consumer.auto-offset-reset=earliest"})
@org.springframework.security.test.context.support.WithMockUser
class TransactionOutboxIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private SpringDataUserRepository userRepository;

	@Autowired
	private SpringDataAccountRepository accountRepository;

	@Autowired
	private SpringDataTransactionRepository transactionRepository;

	@Autowired
	private SpringDataOutboxEventRepository outboxRepository;

	@Autowired
	private SpringDataProcessedEventRepository processedEventRepository;

	@Autowired
	private OutboxEventScheduler outboxEventScheduler;

	private final String userId = UUID.randomUUID().toString();
	private final String accountNumber = "ACC9876543";

	@BeforeEach
	void setUp() {
		// Clear tables first to ensure clean state
		processedEventRepository.deleteAllInBatch();
		outboxRepository.deleteAllInBatch();
		transactionRepository.deleteAllInBatch();
		accountRepository.deleteAllInBatch();
		userRepository.deleteAll();

		// Save test user (Jpa Entity)
		var userEntity = new com.example.demo.user.infrastructure.adapter.out.persistence.entity.UserJpaEntity(
				UUID.fromString(userId), "Integration", "Tester", null);
		var profileEntity = new com.example.demo.user.infrastructure.adapter.out.persistence.entity.ProfileJpaEntity(
				UUID.randomUUID(), userEntity, "Integration", "Tester", null, null);
		userEntity.setProfile(profileEntity);
		userRepository.save(userEntity);

		// Save test account
		var accountEntity = new com.example.demo.account.infrastructure.adapter.out.persistence.entity.AccountJpaEntity(
				UUID.randomUUID(), accountNumber, BigDecimal.ZERO, userId, false);
		accountRepository.save(accountEntity);
	}

	@AfterEach
	void tearDown() {
		processedEventRepository.deleteAllInBatch();
		outboxRepository.deleteAllInBatch();
		transactionRepository.deleteAllInBatch();
		accountRepository.deleteAllInBatch();
		userRepository.deleteAll();
	}

	@Autowired
	private com.example.demo.common.infrastructure.security.JwtService jwtService;

	@Test
	void shouldProcessDepositOutboxEventAsynchronously() throws Exception {
		String jsonPayload = """
				{
				    "accountNumber": "ACC9876543",
				    "amount": 250.00
				}
				""";

		org.springframework.security.core.userdetails.UserDetails userDetails = org.springframework.security.core.userdetails.User
				.builder().username("Integration").password("Tester").authorities("USER").build();
		String token = jwtService.generateToken(userDetails);

		// 1. Act: Call deposit endpoint
		mockMvc.perform(
				post("/api/transactions/deposit").header("X-User-Id", userId).header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
				.andExpect(status().isAccepted());

		// 2. Assert Immediately: Balance is NOT updated yet (it's pending/asynchronous)
		var accountBefore = accountRepository.findByAccountNumber(accountNumber);
		assertTrue(accountBefore.isPresent());
		assertEquals(0, BigDecimal.ZERO.compareTo(accountBefore.get().getBalance()));

		// 3. Assert Outbox Record created
		List<OutboxEventJpaEntity> pendingOutbox = outboxRepository.findAll().stream()
				.filter(e -> e.getStatus() == OutboxStatus.PENDING).toList();
		assertFalse(pendingOutbox.isEmpty(), "Outbox event should be created in PENDING state");
		OutboxEventJpaEntity pendingEvent = pendingOutbox.get(0);
		assertEquals(EventType.TRANSACTION_PENDING, pendingEvent.getEventType());

		// 4. Act: Manually trigger the asynchronous outbox processor to run and
		// complete the update deterministically
		outboxEventScheduler.publishPendingEvents();

		// Wait for Kafka consumer to process the event asynchronously (max 5 seconds)
		org.awaitility.Awaitility.await().atMost(5, java.util.concurrent.TimeUnit.SECONDS).untilAsserted(() -> {
			var accountAfter = accountRepository.findByAccountNumber(accountNumber);
			assertTrue(accountAfter.isPresent());
			// Balance must be updated to 250.00
			assertEquals(0, new BigDecimal("250.00").compareTo(accountAfter.get().getBalance()),
					"Balance should be updated to 250.00 after manual outbox trigger");
		});
		// 5. Assert final state in Database
		// Outbox event status should be PUBLISHED
		Optional<OutboxEventJpaEntity> outboxEventAfter = outboxRepository.findById(pendingEvent.getId());
		assertTrue(outboxEventAfter.isPresent());
		assertEquals(OutboxStatus.PUBLISHED, outboxEventAfter.get().getStatus());

		// Processed event record exists (Idempotency table filled)
		Optional<ProcessedEventJpaEntity> processedEvent = processedEventRepository.findById(pendingEvent.getId());
		assertTrue(processedEvent.isPresent(), "Event should be recorded in processed_events table");
	}
}
