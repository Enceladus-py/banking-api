package com.example.demo.infrastructure.adapter.in.web;

import com.example.demo.application.port.in.RegisterUserUseCase;
import com.example.demo.domain.model.User;
import com.example.demo.infrastructure.adapter.in.web.dto.RegisterUserRequest;
import com.example.demo.infrastructure.adapter.in.web.dto.UserResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final RegisterUserUseCase registerUserUseCase;

    @PostMapping
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        RegisterUserUseCase.RegisterUserCommand command = new RegisterUserUseCase.RegisterUserCommand(request.name(),
                request.surname());

        User user = registerUserUseCase.registerUser(command);
        return ResponseEntity.ok(new UserResponse(user.getId(), user.getName(), user.getSurname()));
    }
}