package com.fintech.banking.coreapi.transaction.application.port.in;

import com.fintech.banking.coreapi.transaction.domain.event.TransactionPendingEvent;

/**
 * Inbound port interface for processing pending transaction events.
 */
public interface ProcessTransactionUseCase {
	/**
	 * Processes the specified pending transaction event.
	 *
	 * @param event
	 *            the pending transaction event
	 */
	void process(TransactionPendingEvent event);
}
