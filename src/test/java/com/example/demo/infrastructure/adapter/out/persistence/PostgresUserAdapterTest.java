package com.example.demo.infrastructure.adapter.out.persistence;

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

import com.example.demo.domain.model.User;
import com.example.demo.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import com.example.demo.infrastructure.adapter.out.persistence.repository.SpringDataUserRepository;

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
		User user = new User(id, "John", "Doe", 1L);
		UserJpaEntity savedEntity = new UserJpaEntity(UUID.fromString(id), "John", "Doe", 1L);

		when(repository.save(any(UserJpaEntity.class))).thenReturn(savedEntity);

		User savedUser = adapter.save(user);

		assertEquals(id, savedUser.getId());
		assertEquals("John", savedUser.getName());
		assertEquals("Doe", savedUser.getSurname());
		assertEquals(1L, savedUser.getVersion());

		ArgumentCaptor<UserJpaEntity> captor = ArgumentCaptor.forClass(UserJpaEntity.class);
		verify(repository, times(1)).save(captor.capture());
		UserJpaEntity captured = captor.getValue();
		assertEquals(UUID.fromString(id), captured.getId());
		assertEquals("John", captured.getName());
		assertEquals("Doe", captured.getSurname());
	}

	@Test
	void shouldFindUserById() {
		String id = UUID.randomUUID().toString();
		UserJpaEntity entity = new UserJpaEntity(UUID.fromString(id), "Jane", "Smith", 2L);

		when(repository.findById(UUID.fromString(id))).thenReturn(Optional.of(entity));

		Optional<User> result = adapter.findById(id);

		assertTrue(result.isPresent());
		assertEquals(id, result.get().getId());
		assertEquals("Jane", result.get().getName());
		assertEquals("Smith", result.get().getSurname());
		assertEquals(2L, result.get().getVersion());
	}

	@Test
	void shouldReturnEmptyWhenUserNotFound() {
		String id = UUID.randomUUID().toString();

		when(repository.findById(UUID.fromString(id))).thenReturn(Optional.empty());

		Optional<User> result = adapter.findById(id);

		assertTrue(result.isEmpty());
	}
}
