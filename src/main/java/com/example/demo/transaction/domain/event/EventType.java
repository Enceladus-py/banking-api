package com.example.demo.transaction.domain.event;

/**
 * Enumeration of transaction event types.
 */
public enum EventType {
	/**
	 * Event emitted when a transaction is pending.
	 */
	TRANSACTION_PENDING,

	/**
	 * Event emitted when a transaction has completed successfully.
	 */
	TRANSACTION_COMPLETED,

	/**
	 * Event emitted when a transaction has failed.
	 */
	TRANSACTION_FAILED
}
