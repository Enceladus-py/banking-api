package com.example.demo.transaction.application.port.out;

import java.util.UUID;

/**
 * Outbound port for managing idempotency through processed events.
 */
public interface ProcessedEventPort {

	/**
	 * Saves the event ID as processed if it doesn't already exist.
	 *
	 * @param eventId
	 *            the unique ID of the event
	 * @return true if successfully saved (first time), false if already exists
	 */
	boolean saveIfAbsent(UUID eventId);
}
