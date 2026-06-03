package com.example.demo.user.infrastructure.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.example.demo.user.application.port.out.UserRepository;
import com.example.demo.user.domain.model.User;
import com.example.demo.user.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import com.example.demo.user.infrastructure.adapter.out.persistence.repository.SpringDataUserRepository;

/**
 * Persistence adapter implementing UserRepository for PostgreSQL database.
 */
@Component
public class PostgresUserAdapter implements UserRepository {

	private final SpringDataUserRepository repository;

	/**
	 * Constructs a new PostgresUserAdapter with the specified Spring Data
	 * repository.
	 *
	 * @param repository
	 *            the Spring Data JPA repository
	 */
	public PostgresUserAdapter(SpringDataUserRepository repository) {
		this.repository = repository;
	}

	@Override
	public User save(User user) {
		UserJpaEntity entity = new UserJpaEntity(UUID.fromString(user.getId()), user.getName(), user.getSurname(),
				user.getVersion());
		UserJpaEntity saved = repository.save(entity);
		return new User(saved.getId().toString(), saved.getName(), saved.getSurname(), saved.getVersion());
	}

	@Override
	public Optional<User> findById(String id) {
		return repository.findById(UUID.fromString(id)).map(entity -> new User(entity.getId().toString(),
				entity.getName(), entity.getSurname(), entity.getVersion()));
	}
}
