package com.fintech.banking.coreapi.common.domain.exception;

/**
 * Exception thrown when a requested domain entity (e.g., user, account) is not
 * found.
 */
public class EntityNotFoundException extends DomainException {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new EntityNotFoundException with the specified detail message.
	 *
	 * @param message
	 *            the detail message
	 */
	public EntityNotFoundException(String message) {
		super(message);
	}
}
