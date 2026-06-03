package com.example.demo.common.infrastructure.adapter.in.web.exception;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.example.demo.common.domain.exception.DomainException;
import com.example.demo.common.domain.exception.EntityNotFoundException;

class GlobalExceptionHandlerTest {

	private GlobalExceptionHandler handler;

	@BeforeEach
	void setUp() {
		handler = new GlobalExceptionHandler();
	}

	@Test
	void handleEntityNotFoundException() {
		EntityNotFoundException ex = new EntityNotFoundException("Entity not found");
		ResponseEntity<Map<String, Object>> response = handler.handleEntityNotFoundException(ex);

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("Entity not found", response.getBody().get("message"));
		assertNotNull(response.getBody().get("timestamp"));
	}

	@Test
	void handleDomainException() {
		DomainException ex = new DomainException("Domain error");
		ResponseEntity<Map<String, Object>> response = handler.handleDomainException(ex);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("Domain error", response.getBody().get("message"));
	}

	@Test
	void handleIllegalArgumentException() {
		IllegalArgumentException ex = new IllegalArgumentException("Illegal argument");
		ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgumentException(ex);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("Illegal argument", response.getBody().get("message"));
	}

	@Test
	void handleIllegalStateException() {
		IllegalStateException ex = new IllegalStateException("Illegal state");
		ResponseEntity<Map<String, Object>> response = handler.handleIllegalStateException(ex);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("Illegal state", response.getBody().get("message"));
	}

	@Test
	void handleSecurityException() {
		SecurityException ex = new SecurityException("Forbidden access");
		ResponseEntity<Map<String, Object>> response = handler.handleSecurityException(ex);

		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("Forbidden access", response.getBody().get("message"));
	}

	@Test
	void handleValidationExceptions() {
		MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
		BindingResult bindingResult = mock(BindingResult.class);

		when(ex.getBindingResult()).thenReturn(bindingResult);
		when(bindingResult.getFieldErrors()).thenReturn(List.of(new FieldError("objectName", "field1", "error1"),
				new FieldError("objectName", "field2", "error2")));

		ResponseEntity<Map<String, Object>> response = handler.handleValidationExceptions(ex);

		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("Validation failed", response.getBody().get("message"));

		@SuppressWarnings("unchecked")
		List<String> errors = (List<String>) response.getBody().get("errors");
		assertEquals(2, errors.size());
		assertTrue(errors.contains("field1: error1"));
		assertTrue(errors.contains("field2: error2"));
	}
}
