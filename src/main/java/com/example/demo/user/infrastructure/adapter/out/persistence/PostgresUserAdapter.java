package com.example.demo.user.infrastructure.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.example.demo.user.application.port.out.UserRepository;
import com.example.demo.user.domain.model.Profile;
import com.example.demo.user.domain.model.User;
import com.example.demo.user.infrastructure.adapter.out.persistence.entity.ProfileJpaEntity;
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
		UserJpaEntity entity = new UserJpaEntity(UUID.fromString(user.getId()), user.getEmail(), user.getPassword(),
				user.getVersion());

		ProfileJpaEntity profileEntity = new ProfileJpaEntity(UUID.fromString(user.getProfile().getId()), entity,
				user.getProfile().getName(), user.getProfile().getSurname(), user.getProfile().getMobileNumber(),
				user.getProfile().getAddress());

		entity.setProfile(profileEntity);

		UserJpaEntity saved = repository.save(entity);
		return mapToDomain(saved);
	}

	@Override
	public Optional<User> findById(String id) {
		return repository.findById(UUID.fromString(id)).map(this::mapToDomain);
	}

	@Override
	public Optional<User> findByEmail(String email) {
		return repository.findByEmail(email).map(this::mapToDomain);
	}

	private User mapToDomain(UserJpaEntity entity) {
		Profile profile = new Profile(entity.getProfile().getId().toString(), entity.getProfile().getName(),
				entity.getProfile().getSurname(), entity.getProfile().getMobileNumber(),
				entity.getProfile().getAddress());
		return new User(entity.getId().toString(), entity.getEmail(), entity.getPassword(), profile,
				entity.getVersion());
	}
}
