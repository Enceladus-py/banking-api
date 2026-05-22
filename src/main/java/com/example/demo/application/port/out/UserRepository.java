package com.example.demo.application.port.out;

import com.example.demo.domain.model.User;
import java.util.Optional;

public interface UserRepository {
    User save(User user);

    Optional<User> findById(String id);
}