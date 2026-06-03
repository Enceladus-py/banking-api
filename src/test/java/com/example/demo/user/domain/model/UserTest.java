package com.example.demo.user.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UserTest {

	@Test
	void shouldCreateUserWithGeneratedIdAndTrimmedStrings() {
		User user = new User("  John  ", " Doe ");

		assertNotNull(user.getId());
		assertEquals("John", user.getName());
		assertEquals("Doe", user.getSurname());
	}

	@Test
	void shouldThrowExceptionWhenNameIsBlank() {
		assertThrows(IllegalArgumentException.class, () -> new User("   ", "Doe"));
		assertThrows(IllegalArgumentException.class, () -> new User(null, "Doe"));
	}

	@Test
	void shouldThrowExceptionWhenSurnameIsBlank() {
		assertThrows(IllegalArgumentException.class, () -> new User("John", "   "));
		assertThrows(IllegalArgumentException.class, () -> new User("John", null));
	}
}
