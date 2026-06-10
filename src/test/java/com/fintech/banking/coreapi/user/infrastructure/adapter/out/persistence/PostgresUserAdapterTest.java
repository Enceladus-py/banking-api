package com.fintech.banking.coreapi.user.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fintech.banking.coreapi.user.domain.model.Profile;
import com.fintech.banking.coreapi.user.domain.model.User;
import com.fintech.banking.coreapi.user.infrastructure.adapter.out.persistence.entity.ProfileJpaEntity;
import com.fintech.banking.coreapi.user.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import com.fintech.banking.coreapi.user.infrastructure.adapter.out.persistence.repository.SpringDataUserRepository;

@ExtendWith(MockitoExtension.class)
class PostgresUserAdapterTest {

	@Mock
	private SpringDataUserRepository repository;

	private PostgresUserAdapter adapter;

	@BeforeEach
	void setUp() {
		adapter = new PostgresUserAdapter(repository);
	}

	@Test
	void shouldSaveUserSuccessfully() {
		String id = UUID.randomUUID().toString();
		User user = User.reconstitute(id, "john@example.com", "pwd", Profile.createNew("John", "Doe"), 1L);
		UserJpaEntity savedEntity = new UserJpaEntity(UUID.fromString(id), "john@example.com", "pwd", 1L);
		ProfileJpaEntity profileEntity = new ProfileJpaEntity(UUID.fromString(user.getProfile().getId()), savedEntity,
				"John", "Doe", null, null);
		savedEntity.setProfile(profileEntity);

		when(repository.save(any(UserJpaEntity.class))).thenReturn(savedEntity);

		User savedUser = adapter.save(user);

		assertEquals(id, savedUser.getId());
		assertEquals("john@example.com", savedUser.getEmail());
		assertEquals("John", savedUser.getProfile().getName());
		assertEquals("Doe", savedUser.getProfile().getSurname());
		assertEquals(1L, savedUser.getVersion());

		ArgumentCaptor<UserJpaEntity> captor = ArgumentCaptor.forClass(UserJpaEntity.class);
		verify(repository, times(1)).save(captor.capture());
		UserJpaEntity captured = captor.getValue();
		assertEquals(UUID.fromString(id), captured.getId());
		assertEquals("john@example.com", captured.getEmail());
		assertEquals("John", captured.getProfile().getName());
		assertEquals("Doe", captured.getProfile().getSurname());
	}

	@Test
	void shouldFindUserById() {
		String id = UUID.randomUUID().toString();
		UserJpaEntity entity = new UserJpaEntity(UUID.fromString(id), "jane@example.com", "pwd", 2L);
		ProfileJpaEntity profileEntity = new ProfileJpaEntity(UUID.randomUUID(), entity, "Jane", "Smith", null, null);
		entity.setProfile(profileEntity);

		when(repository.findById(UUID.fromString(id))).thenReturn(Optional.of(entity));

		Optional<User> result = adapter.findById(id);

		assertTrue(result.isPresent());
		assertEquals(id, result.get().getId());
		assertEquals("jane@example.com", result.get().getEmail());
		assertEquals("Jane", result.get().getProfile().getName());
		assertEquals("Smith", result.get().getProfile().getSurname());
		assertEquals(2L, result.get().getVersion());
	}

	@Test
	void shouldFindUserByEmail() {
		String id = UUID.randomUUID().toString();
		UserJpaEntity entity = new UserJpaEntity(UUID.fromString(id), "jane@example.com", "pwd", 2L);
		ProfileJpaEntity profileEntity = new ProfileJpaEntity(UUID.randomUUID(), entity, "Jane", "Smith", null, null);
		entity.setProfile(profileEntity);

		when(repository.findByEmail("jane@example.com")).thenReturn(Optional.of(entity));

		Optional<User> result = adapter.findByEmail("jane@example.com");

		assertTrue(result.isPresent());
		assertEquals("jane@example.com", result.get().getEmail());
	}

	@Test
	void shouldReturnEmptyWhenUserNotFound() {
		String id = UUID.randomUUID().toString();

		when(repository.findById(UUID.fromString(id))).thenReturn(Optional.empty());

		Optional<User> result = adapter.findById(id);

		assertTrue(result.isEmpty());
	}
}
