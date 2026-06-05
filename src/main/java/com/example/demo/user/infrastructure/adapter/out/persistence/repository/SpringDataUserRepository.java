package com.example.demo.user.infrastructure.adapter.out.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.user.infrastructure.adapter.out.persistence.entity.UserJpaEntity;

/**
 * Spring Data JPA repository for {@link UserJpaEntity}.
 */
public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, UUID> {
	/**
	 * Finds a user by their email.
	 *
	 * @param email
	 *            the email to search for
	 * @return an Optional containing the entity if found
	 */
	java.util.Optional<UserJpaEntity> findByEmail(String email);
}
