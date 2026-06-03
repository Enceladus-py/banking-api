package com.example.demo.common.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.stereotype.Component;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"test-dlq-topic", "test-dlq-topic.DLT", "test-dlq-topic-dlt"})
@TestPropertySource(properties = {"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
		"spring.kafka.consumer.auto-offset-reset=earliest", "outbox.scheduler.delay=9999999", // Disable outbox
																								// scheduler
																								// to prevent noise
																								// during test
		"kafka.backoff.initialInterval=2", "kafka.backoff.multiplier=2.0", "kafka.backoff.maxInterval=10",
		"kafka.backoff.maxElapsedTime=20"})
@org.springframework.test.annotation.DirtiesContext(classMode = org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS)
public class KafkaErrorHandlerIntegrationTest {

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	@Autowired
	private DlqTestListener dlqTestListener;

	@Autowired
	private org.springframework.kafka.config.KafkaListenerEndpointRegistry registry;

	@Test
	public void testExponentialBackoffAndDlq() throws Exception {
		// Wait a moment for Kafka consumer to initialize and partition assignment to
		// complete
		for (org.springframework.kafka.listener.MessageListenerContainer container : registry.getListenerContainers()) {
			int expectedPartitions = container.getContainerProperties().getTopics() != null
					? container.getContainerProperties().getTopics().length
					: 1;
			org.springframework.kafka.test.utils.ContainerTestUtils.waitForAssignment(container, expectedPartitions);
		}

		// Send message to the test topic
		kafkaTemplate.send("test-dlq-topic", "test-payload").get(); // .get() to ensure it's sent

		// Wait for the message to hit the DLQ topic.
		// Since we have an exponential backoff (1s, 2s, 4s, 8s max 15s elapsed),
		// it will take around 15 seconds to exhaust retries and go to DLQ.
		await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
			assertThat(dlqTestListener.isDlqReceived()).isTrue();
		});

		// Verify it was retried multiple times
		assertThat(dlqTestListener.getAttemptCount().get()).isGreaterThanOrEqualTo(3);
	}

	@Component
	public static class DlqTestListener {
		private final AtomicInteger attemptCount = new AtomicInteger(0);
		private volatile boolean dlqReceived = false;

		@KafkaListener(topics = "test-dlq-topic", groupId = "dlq-test-group")
		public void listen(String message) {
			int attempt = attemptCount.incrementAndGet();
			System.err.println(">>>> Processing message... Attempt: " + attempt + " at " + System.currentTimeMillis());
			throw new RuntimeException("Simulated failure to trigger backoff!");
		}

		@KafkaListener(topics = {"test-dlq-topic.DLT", "test-dlq-topic-dlt"}, groupId = "dlq-test-dlt-group")
		public void listenDlq(ConsumerRecord<String, String> record) {
			System.err.println(
					">>>> Message finally landed in DLQ at " + System.currentTimeMillis() + ": " + record.value());
			dlqReceived = true;
		}

		public AtomicInteger getAttemptCount() {
			return attemptCount;
		}

		public boolean isDlqReceived() {
			return dlqReceived;
		}
	}
}
