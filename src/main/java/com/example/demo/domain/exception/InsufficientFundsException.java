package com.example.demo.domain.exception;

public class InsufficientFundsException extends DomainException {
	private static final long serialVersionUID = 1L;

	public InsufficientFundsException(String message) {
		super(message);
	}
}
