package com.example.demo.user.infrastructure.adapter.out.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.user.infrastructure.adapter.out.persistence.entity.UserJpaEntity;

/**
 * Spring Data JPA repository for {@link UserJpaEntity}.
 */
public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, UUID> {
}
