package com.fintech.banking.coreapi.common.domain.exception;

/**
 * Exception thrown when user is not permitted.
 */
public class AccessDeniedException extends DomainException {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new AccessDeniedException with the specified detail message.
	 *
	 * @param message
	 *            the detail message
	 */
	public AccessDeniedException(String message) {
		super(message);
	}
}
