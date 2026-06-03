package com.example.demo.application.port.out;

import java.util.Optional;

import com.example.demo.domain.model.User;

public interface UserRepository {
	User save(User user);

	Optional<User> findById(String id);
}
