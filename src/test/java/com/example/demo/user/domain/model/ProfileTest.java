package com.example.demo.user.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ProfileTest {

	@Test
	void shouldCreateProfileWithTrimmedNameAndSurname() {
		Profile profile = new Profile("  John  ", "  Doe  ");
		assertEquals("John", profile.getName());
		assertEquals("Doe", profile.getSurname());
		assertNotNull(profile.getId());
		assertNull(profile.getMobileNumber());
		assertNull(profile.getAddress());
	}

	@Test
	void shouldThrowExceptionWhenNameIsBlank() {
		assertThrows(IllegalArgumentException.class, () -> new Profile("   ", "Doe"));
		assertThrows(IllegalArgumentException.class, () -> new Profile(null, "Doe"));
	}

	@Test
	void shouldThrowExceptionWhenSurnameIsBlank() {
		assertThrows(IllegalArgumentException.class, () -> new Profile("John", "   "));
		assertThrows(IllegalArgumentException.class, () -> new Profile("John", null));
	}

	@Test
	void shouldCreateProfileWithMobileNumberAndAddress() {
		Profile profile = new Profile("id-123", "  John  ", "  Doe  ", "  +123456  ", "  123 Main St  ");
		assertEquals("id-123", profile.getId());
		assertEquals("John", profile.getName());
		assertEquals("Doe", profile.getSurname());
		assertEquals("+123456", profile.getMobileNumber());
		assertEquals("123 Main St", profile.getAddress());
	}

	@Test
	void shouldUpdateMobileNumberAndAddress() {
		Profile profile = new Profile("John", "Doe");
		profile.update("  +987654  ", "  456 Oak St  ");
		assertEquals("+987654", profile.getMobileNumber());
		assertEquals("456 Oak St", profile.getAddress());
	}

	@Test
	void shouldUpdateMobileNumberOnly() {
		Profile profile = new Profile("id-123", "John", "Doe", "+123456", "123 Main St");
		profile.update("  +987654  ", null);
		assertEquals("+987654", profile.getMobileNumber());
		assertEquals("123 Main St", profile.getAddress());
	}

	@Test
	void shouldUpdateAddressOnly() {
		Profile profile = new Profile("id-123", "John", "Doe", "+123456", "123 Main St");
		profile.update(null, "  456 Oak St  ");
		assertEquals("+123456", profile.getMobileNumber());
		assertEquals("456 Oak St", profile.getAddress());
	}

	@Test
	void shouldNotUpdateWhenValuesAreNull() {
		Profile profile = new Profile("id-123", "John", "Doe", "+123456", "123 Main St");
		profile.update(null, null);
		assertEquals("+123456", profile.getMobileNumber());
		assertEquals("123 Main St", profile.getAddress());
	}
}
