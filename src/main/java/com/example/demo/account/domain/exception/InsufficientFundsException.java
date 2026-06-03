package com.example.demo.account.domain.exception;
import com.example.demo.common.domain.exception.DomainException;

/**
 * Exception thrown when an account does not have sufficient funds for
 * withdrawal or transfer.
 */
public class InsufficientFundsException extends DomainException {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new InsufficientFundsException with the specified detail
	 * message.
	 *
	 * @param message
	 *            the detail message
	 */
	public InsufficientFundsException(String message) {
		super(message);
	}
}
