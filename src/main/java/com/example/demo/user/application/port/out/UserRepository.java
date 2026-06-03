package com.example.demo.user.application.port.out;

import java.util.Optional;

import com.example.demo.user.domain.model.User;

public interface UserRepository {
	User save(User user);

	Optional<User> findById(String id);
}
