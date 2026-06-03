package com.example.demo.transaction.infrastructure.adapter.out.persistence.entity;

/**
 * Enumeration representing the dispatch status of outbox events.
 */
public enum OutboxStatus {
	/**
	 * Event is pending delivery.
	 */
	PENDING,

	/**
	 * Event has been successfully published to the message broker.
	 */
	PUBLISHED,

	/**
	 * Event publication failed.
	 */
	FAILED
}
