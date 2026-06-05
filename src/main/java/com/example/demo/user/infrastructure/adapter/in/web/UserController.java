package com.example.demo.user.infrastructure.adapter.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.user.application.port.in.GetUserUseCase;
import com.example.demo.user.application.port.in.RegisterUserUseCase;
import com.example.demo.user.domain.model.User;
import com.example.demo.user.infrastructure.adapter.in.web.dto.RegisterUserRequest;
import com.example.demo.user.infrastructure.adapter.in.web.dto.UserResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Controller for user registration and management endpoints.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Endpoints for user registration and profile management")
public class UserController {

	private final RegisterUserUseCase registerUserUseCase;
	private final GetUserUseCase getUserUseCase;

	/**
	 * Constructs a new UserController with the required use cases.
	 *
	 * @param registerUserUseCase
	 *            the use case to register users
	 * @param getUserUseCase
	 *            the use case to retrieve user details
	 */
	public UserController(RegisterUserUseCase registerUserUseCase, GetUserUseCase getUserUseCase) {
		this.registerUserUseCase = registerUserUseCase;
		this.getUserUseCase = getUserUseCase;
	}

	/**
	 * Registers a new user in the system.
	 *
	 * @param request
	 *            the user registration details request payload
	 * @return the response containing user details
	 */
	@PostMapping("/register")
	@Operation(summary = "Register user", description = "Registers a new customer in the system with their email, password, name and surname")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "User successfully registered"),
			@ApiResponse(responseCode = "400", description = "Invalid request validation failed")})
	public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
		RegisterUserUseCase.RegisterUserCommand command = new RegisterUserUseCase.RegisterUserCommand(
				request.email(), request.password(), request.name(), request.surname());

		User user = registerUserUseCase.registerUser(command);
		return ResponseEntity.ok(new UserResponse(user.getId(), user.getEmail(), user.getProfile().getName(), user.getProfile().getSurname()));
	}

	/**
	 * Retrieves the user profile by ID.
	 *
	 * @param id
	 *            the user ID
	 * @return the response containing user details
	 */
	@GetMapping("/{id}")
	@Operation(summary = "Get user profile", description = "Retrieves user details by their identifier")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "User found"),
			@ApiResponse(responseCode = "404", description = "User not found")})
	public ResponseEntity<UserResponse> getUserById(
			@PathVariable @Parameter(description = "The unique identifier of the user", example = "123e4567-e89b-12d3-a456-426614174000") String id) {
		User user = getUserUseCase.getUserById(id);
		return ResponseEntity.ok(new UserResponse(user.getId(), user.getEmail(), user.getProfile().getName(), user.getProfile().getSurname()));
	}
}
