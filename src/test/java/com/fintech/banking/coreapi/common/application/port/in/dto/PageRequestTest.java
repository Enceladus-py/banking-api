package com.fintech.banking.coreapi.common.application.port.in.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PageRequestTest {

	@Test
	void shouldCreateValidPageRequest() {
		PageRequest request = new PageRequest(0, 10);
		assertEquals(0, request.pageNumber());
		assertEquals(10, request.pageSize());
	}

	@Test
	void shouldThrowExceptionWhenPageNumberIsNegative() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
			new PageRequest(-1, 10);
		});
		assertEquals("Page number cannot be less than zero", exception.getMessage());
	}

	@Test
	void shouldThrowExceptionWhenPageSizeIsInvalid() {
		IllegalArgumentException exception1 = assertThrows(IllegalArgumentException.class, () -> {
			new PageRequest(0, 0);
		});
		assertEquals("Page size must not be less than one", exception1.getMessage());

		IllegalArgumentException exception2 = assertThrows(IllegalArgumentException.class, () -> {
			new PageRequest(0, -5);
		});
		assertEquals("Page size must not be less than one", exception2.getMessage());

		IllegalArgumentException exception3 = assertThrows(IllegalArgumentException.class, () -> {
			new PageRequest(0, 101);
		});
		assertEquals("Page size cannot exceed 100", exception3.getMessage());
	}
}
