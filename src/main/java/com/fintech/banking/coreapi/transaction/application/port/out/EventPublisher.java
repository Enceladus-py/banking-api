package com.fintech.banking.coreapi.transaction.application.port.out;

import com.fintech.banking.coreapi.transaction.domain.event.TransactionEvent;

/**
 * Outbound port interface for publishing transaction domain events.
 */
public interface EventPublisher {
	/**
	 * Publishes the specified transaction domain event.
	 *
	 * @param event
	 *            the transaction event to publish
	 */
	void publish(TransactionEvent event);
}
