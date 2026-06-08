package com.example.demo.user.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UserTest {

	@Test
	void shouldCreateUserWithGeneratedIdAndTrimmedStrings() {
		User user = new User("  john@example.com  ", " password ", "  John  ", " Doe ");

		assertNotNull(user.getId());
		assertEquals("john@example.com", user.getEmail());
		assertEquals(" password ", user.getPassword());
		assertEquals("John", user.getProfile().getName());
		assertEquals("Doe", user.getProfile().getSurname());
	}

	@Test
	void shouldThrowExceptionWhenEmailIsBlank() {
		assertThrows(IllegalArgumentException.class, () -> new User("   ", "password", "John", "Doe"));
		assertThrows(IllegalArgumentException.class, () -> new User(null, "password", "John", "Doe"));
	}

	@Test
	void shouldThrowExceptionWhenPasswordIsBlank() {
		assertThrows(IllegalArgumentException.class, () -> new User("john@example.com", "   ", "John", "Doe"));
		assertThrows(IllegalArgumentException.class, () -> new User("john@example.com", null, "John", "Doe"));
	}

	@Test
	void shouldThrowExceptionWhenNameIsBlank() {
		assertThrows(IllegalArgumentException.class, () -> new User("john@example.com", "password", "   ", "Doe"));
		assertThrows(IllegalArgumentException.class, () -> new User("john@example.com", "password", null, "Doe"));
	}

	@Test
	void shouldThrowExceptionWhenSurnameIsBlank() {
		assertThrows(IllegalArgumentException.class, () -> new User("john@example.com", "password", "John", "   "));
		assertThrows(IllegalArgumentException.class, () -> new User("john@example.com", "password", "John", null));
	}

	@Test
	void shouldThrowExceptionWhenProfileIsNull() {
		assertThrows(IllegalArgumentException.class, () -> new User("uuid", "john@example.com", "password", null, 1L));
	}
}
