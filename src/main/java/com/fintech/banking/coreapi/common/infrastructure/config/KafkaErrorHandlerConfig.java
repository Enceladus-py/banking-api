package com.fintech.banking.coreapi.common.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

/**
 * Configuration class for Kafka error handling and retry policies.
 */
@Configuration
public class KafkaErrorHandlerConfig {

	/**
	 * Default constructor.
	 */
	public KafkaErrorHandlerConfig() {
	}

	/**
	 * Configures a DefaultErrorHandler with exponential backoff and DLQ recovery.
	 *
	 * @param kafkaTemplate
	 *            the template used to publish to DLQ
	 * @param initialInterval
	 *            the initial wait duration in milliseconds
	 * @param multiplier
	 *            the multiplication factor for subsequent retries
	 * @param maxInterval
	 *            the maximum interval duration in milliseconds
	 * @param maxElapsedTime
	 *            the maximum total elapsed retry time in milliseconds
	 * @return the configured DefaultErrorHandler bean
	 */
	@Bean
	public DefaultErrorHandler errorHandler(KafkaTemplate<String, String> kafkaTemplate,
			@org.springframework.beans.factory.annotation.Value("${kafka.backoff.initialInterval:1000}") long initialInterval,
			@org.springframework.beans.factory.annotation.Value("${kafka.backoff.multiplier:2.0}") double multiplier,
			@org.springframework.beans.factory.annotation.Value("${kafka.backoff.maxInterval:10000}") long maxInterval,
			@org.springframework.beans.factory.annotation.Value("${kafka.backoff.maxElapsedTime:15000}") long maxElapsedTime) {
		// Recoverer sends failed messages to the original topic name with a ".DLT"
		// suffix (e.g. transaction-events.DLT)
		DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);

		// Configure Exponential Backoff
		ExponentialBackOff backOff = new ExponentialBackOff();
		backOff.setInitialInterval(initialInterval); // Wait 1 second before first retry (default)
		backOff.setMultiplier(multiplier); // Double the delay each time (default)
		backOff.setMaxInterval(maxInterval); // Cap maximum wait time between retries to 10 seconds (default)
		backOff.setMaxElapsedTime(maxElapsedTime); // Stop retrying and send to DLQ after 15 seconds of total elapsed
													// time (default)

		return new DefaultErrorHandler(recoverer, backOff);
	}
}
