package com.fintech.banking.coreapi.common.infrastructure.adapter.in.web.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.fintech.banking.coreapi.common.domain.exception.AccessDeniedException;
import com.fintech.banking.coreapi.common.domain.exception.DomainException;
import com.fintech.banking.coreapi.common.domain.exception.EntityNotFoundException;
import com.fintech.banking.coreapi.common.infrastructure.adapter.in.web.dto.ErrorResponse;

class GlobalExceptionHandlerTest {

	private GlobalExceptionHandler handler;

	@BeforeEach
	void setUp() {
		handler = new GlobalExceptionHandler();
	}

	@Test
	void handleEntityNotFoundException() {
		EntityNotFoundException ex = new EntityNotFoundException("Entity not found");
		ResponseEntity<ErrorResponse> response = handler.handleEntityNotFoundException(ex);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("Entity not found", response.getBody().message());
		assertNotNull(response.getBody().timestamp());
	}

	@Test
	void handleDomainException() {
		DomainException ex = new DomainException("Domain error");
		ResponseEntity<ErrorResponse> response = handler.handleDomainException(ex);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("Domain error", response.getBody().message());
	}

	@Test
	void handleIllegalArgumentException() {
		IllegalArgumentException ex = new IllegalArgumentException("Illegal argument");
		ResponseEntity<ErrorResponse> response = handler.handleIllegalArgumentException(ex);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("Illegal argument", response.getBody().message());
	}

	@Test
	void handleIllegalStateException() {
		IllegalStateException ex = new IllegalStateException("Illegal state");
		ResponseEntity<ErrorResponse> response = handler.handleIllegalStateException(ex);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("Illegal state", response.getBody().message());
	}

	@Test
	void handleAccessDeniedException() {
		AccessDeniedException ex = new AccessDeniedException("Forbidden access");
		ResponseEntity<ErrorResponse> response = handler.handleAccessDeniedException(ex);

		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("Forbidden access", response.getBody().message());
	}

	@Test
	void handleValidationExceptions() {
		MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
		BindingResult bindingResult = mock(BindingResult.class);

		when(ex.getBindingResult()).thenReturn(bindingResult);
		when(bindingResult.getFieldErrors()).thenReturn(List.of(new FieldError("objectName", "field1", "error1"),
				new FieldError("objectName", "field2", "error2")));

		ResponseEntity<ErrorResponse> response = handler.handleValidationExceptions(ex);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("Validation failed", response.getBody().message());

		List<String> errors = response.getBody().errors();
		assertEquals(2, errors.size());
		assertTrue(errors.contains("field1: error1"));
		assertTrue(errors.contains("field2: error2"));
	}
}
