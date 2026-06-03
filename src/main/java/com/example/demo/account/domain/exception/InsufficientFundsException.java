package com.example.demo.account.domain.exception;
import com.example.demo.common.domain.exception.DomainException;

public class InsufficientFundsException extends DomainException {
	private static final long serialVersionUID = 1L;

	public InsufficientFundsException(String message) {
		super(message);
	}
}
