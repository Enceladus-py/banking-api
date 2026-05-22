package com.example.demo.infrastructure.adapter.out.persistence;

import com.example.demo.application.port.out.UserRepository;
import com.example.demo.domain.model.User;
import com.example.demo.infrastructure.adapter.out.persistence.entity.UserJpaEntity;
import com.example.demo.infrastructure.adapter.out.persistence.repository.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PostgresUserAdapter implements UserRepository {

    private final SpringDataUserRepository repository;

    @Override
    public User save(User user) {
        UserJpaEntity entity = new UserJpaEntity(
                UUID.fromString(user.getId()),
                user.getName(),
                user.getSurname());
        UserJpaEntity saved = repository.save(entity);
        return new User(saved.getId().toString(), saved.getName(), saved.getSurname());
    }

    @Override
    public Optional<User> findById(String id) {
        return repository.findById(UUID.fromString(id))
                .map(entity -> new User(entity.getId().toString(), entity.getName(), entity.getSurname()));
    }
}