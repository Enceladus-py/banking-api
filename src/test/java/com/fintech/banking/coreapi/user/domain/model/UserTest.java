package com.fintech.banking.coreapi.user.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UserTest {

	@Test
	void shouldCreateUserWithGeneratedIdAndTrimmedStrings() {
		User user = User.createNew("  john@example.com  ", " password ", "  John  ", " Doe ");

		assertNotNull(user.getId());
		assertEquals("john@example.com", user.getEmail());
		assertEquals(" password ", user.getPassword());
		assertEquals("John", user.getProfile().getName());
		assertEquals("Doe", user.getProfile().getSurname());
	}

	@Test
	void shouldThrowExceptionWhenEmailIsBlank() {
		assertThrows(IllegalArgumentException.class, () -> User.createNew("   ", "password", "John", "Doe"));
		assertThrows(IllegalArgumentException.class, () -> User.createNew(null, "password", "John", "Doe"));
	}

	@Test
	void shouldThrowExceptionWhenPasswordIsBlank() {
		assertThrows(IllegalArgumentException.class, () -> User.createNew("john@example.com", "   ", "John", "Doe"));
		assertThrows(IllegalArgumentException.class, () -> User.createNew("john@example.com", null, "John", "Doe"));
	}

	@Test
	void shouldThrowExceptionWhenNameIsBlank() {
		assertThrows(IllegalArgumentException.class,
				() -> User.createNew("john@example.com", "password", "   ", "Doe"));
		assertThrows(IllegalArgumentException.class, () -> User.createNew("john@example.com", "password", null, "Doe"));
	}

	@Test
	void shouldThrowExceptionWhenSurnameIsBlank() {
		assertThrows(IllegalArgumentException.class,
				() -> User.createNew("john@example.com", "password", "John", "   "));
		assertThrows(IllegalArgumentException.class,
				() -> User.createNew("john@example.com", "password", "John", null));
	}

	@Test
	void shouldThrowExceptionWhenProfileIsNull() {
		assertThrows(IllegalArgumentException.class,
				() -> User.reconstitute("uuid", "john@example.com", "password", null, 1L));
	}
}
