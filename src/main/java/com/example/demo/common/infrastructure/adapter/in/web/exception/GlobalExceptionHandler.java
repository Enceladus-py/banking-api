package com.example.demo.common.infrastructure.adapter.in.web.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.demo.common.domain.exception.DomainException;
import com.example.demo.common.domain.exception.EntityNotFoundException;

/**
 * Global REST controller advice to handle all application exceptions and map
 * them to HTTP responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * Default constructor.
	 */
	public GlobalExceptionHandler() {
	}

	/**
	 * Handles EntityNotFoundException and returns 404 status.
	 *
	 * @param ex
	 *            the exception
	 * @return response containing error details
	 */
	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleEntityNotFoundException(EntityNotFoundException ex) {
		Map<String, Object> body = new HashMap<>();
		body.put("timestamp", LocalDateTime.now());
		body.put("message", ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
	}

	/**
	 * Handles DomainException and returns 400 status.
	 *
	 * @param ex
	 *            the exception
	 * @return response containing error details
	 */
	@ExceptionHandler(DomainException.class)
	public ResponseEntity<Map<String, Object>> handleDomainException(DomainException ex) {
		Map<String, Object> body = new HashMap<>();
		body.put("timestamp", LocalDateTime.now());
		body.put("message", ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	/**
	 * Handles IllegalArgumentException and returns 400 status.
	 *
	 * @param ex
	 *            the exception
	 * @return response containing error details
	 */
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
		Map<String, Object> body = new HashMap<>();
		body.put("timestamp", LocalDateTime.now());
		body.put("message", ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	/**
	 * Handles IllegalStateException and returns 400 status.
	 *
	 * @param ex
	 *            the exception
	 * @return response containing error details
	 */
	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<Map<String, Object>> handleIllegalStateException(IllegalStateException ex) {
		Map<String, Object> body = new HashMap<>();
		body.put("timestamp", LocalDateTime.now());
		body.put("message", ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	/**
	 * Handles SecurityException and returns 403 status.
	 *
	 * @param ex
	 *            the exception
	 * @return response containing error details
	 */
	@ExceptionHandler(SecurityException.class)
	public ResponseEntity<Map<String, Object>> handleSecurityException(SecurityException ex) {
		Map<String, Object> body = new HashMap<>();
		body.put("timestamp", LocalDateTime.now());
		body.put("message", ex.getMessage());

		// Returns 403 Forbidden when a user tries to touch an account they don't own
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
	}

	/**
	 * Handles validation exceptions and returns 400 status with details.
	 *
	 * @param ex
	 *            the validation exception
	 * @return response containing error details
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {

		Map<String, Object> body = new HashMap<>();
		body.put("timestamp", LocalDateTime.now());
		body.put("message", "Validation failed");

		// Extract all the specific field errors into a clean list
		java.util.List<String> errors = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage()).toList();

		body.put("errors", errors);

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}
}
