package com.fintech.banking.coreapi.common.domain.exception;

/**
 * Base exception class for all domain-specific exceptions.
 */
public class DomainException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new DomainException with the specified detail message.
	 *
	 * @param message
	 *            the detail message
	 */
	public DomainException(String message) {
		super(message);
	}
}
