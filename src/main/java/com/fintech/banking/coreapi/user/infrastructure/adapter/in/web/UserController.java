package com.fintech.banking.coreapi.user.infrastructure.adapter.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import com.fintech.banking.coreapi.common.infrastructure.security.CustomUserDetails;
import com.fintech.banking.coreapi.common.infrastructure.security.JwtService;
import com.fintech.banking.coreapi.user.application.port.in.GetUserUseCase;
import com.fintech.banking.coreapi.user.application.port.in.RegisterUserUseCase;
import com.fintech.banking.coreapi.user.application.port.in.UpdateProfileUseCase;
import com.fintech.banking.coreapi.user.domain.model.User;
import com.fintech.banking.coreapi.user.infrastructure.adapter.in.web.dto.LoginRequest;
import com.fintech.banking.coreapi.user.infrastructure.adapter.in.web.dto.LoginResponse;
import com.fintech.banking.coreapi.user.infrastructure.adapter.in.web.dto.ProfileUpdateRequest;
import com.fintech.banking.coreapi.user.infrastructure.adapter.in.web.dto.RefreshTokenRequest;
import com.fintech.banking.coreapi.user.infrastructure.adapter.in.web.dto.RegisterUserRequest;
import com.fintech.banking.coreapi.user.infrastructure.adapter.in.web.dto.UserResponse;

import io.swagger.v3.oas.annotations.Operation;
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
	private final UpdateProfileUseCase updateProfileUseCase;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final UserDetailsService userDetailsService;

	/**
	 * Constructs a new UserController with the required use cases.
	 *
	 * @param registerUserUseCase
	 *            the use case to register users
	 * @param getUserUseCase
	 *            the use case to retrieve user details
	 * @param updateProfileUseCase
	 *            the use case to update user profile
	 * @param authenticationManager
	 *            the authentication manager
	 * @param jwtService
	 *            the JWT service
	 * @param userDetailsService
	 *            the user details service
	 */
	public UserController(RegisterUserUseCase registerUserUseCase, GetUserUseCase getUserUseCase,
			UpdateProfileUseCase updateProfileUseCase, AuthenticationManager authenticationManager,
			JwtService jwtService, UserDetailsService userDetailsService) {
		this.registerUserUseCase = registerUserUseCase;
		this.getUserUseCase = getUserUseCase;
		this.updateProfileUseCase = updateProfileUseCase;
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
	}

	/**
	 * Authenticates a user and returns a JWT token.
	 *
	 * @param request
	 *            the login request containing email and password
	 * @return the response containing the JWT tokens
	 */
	@PostMapping("/login")
	@Operation(summary = "Login user", description = "Authenticates a user and returns JWT access and refresh tokens")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Successfully authenticated"),
			@ApiResponse(responseCode = "401", description = "Invalid credentials")})
	public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		Authentication authentication = authenticationManager
				.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
		UserDetails userDetails = (UserDetails) authentication.getPrincipal();
		String jwt = jwtService.generateToken(userDetails);
		String refreshToken = jwtService.generateRefreshToken(userDetails);
		return ResponseEntity.ok(new LoginResponse(jwt, refreshToken));
	}

	/**
	 * Refreshes an access token using a refresh token.
	 *
	 * @param request
	 *            the refresh token request
	 * @return the response containing the new JWT tokens
	 */
	@PostMapping("/refresh")
	@Operation(summary = "Refresh token", description = "Refreshes a JWT access token using a refresh token")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Successfully refreshed"),
			@ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")})
	public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
		String refreshToken = request.refreshToken();
		String username = jwtService.extractUsername(refreshToken);

		if (username != null) {
			UserDetails userDetails = userDetailsService.loadUserByUsername(username);
			if (jwtService.isTokenValid(refreshToken, userDetails)) {
				String newAccessToken = jwtService.generateToken(userDetails);
				String newRefreshToken = jwtService.generateRefreshToken(userDetails);
				return ResponseEntity.ok(new LoginResponse(newAccessToken, newRefreshToken));
			}
		}
		return ResponseEntity.status(401).build();
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
		RegisterUserUseCase.RegisterUserCommand command = new RegisterUserUseCase.RegisterUserCommand(request.email(),
				request.password(), request.name(), request.surname());

		User user = registerUserUseCase.registerUser(command);
		return ResponseEntity.ok(toUserResponse(user));
	}

	/**
	 * Retrieves the authenticated user's profile.
	 *
	 * @param userDetails
	 *            the authenticated user details
	 * @return the response containing user details
	 */
	@GetMapping("/profile")
	@Operation(summary = "Get user profile", description = "Retrieves user details for the currently authenticated user")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "User found"),
			@ApiResponse(responseCode = "401", description = "Unauthorized"),
			@ApiResponse(responseCode = "404", description = "User not found")})
	public ResponseEntity<UserResponse> getUserProfile(@AuthenticationPrincipal CustomUserDetails userDetails) {
		User user = getUserUseCase.getUserById(userDetails.getId());
		return ResponseEntity.ok(toUserResponse(user));
	}

	/**
	 * Updates the authenticated user's profile details.
	 *
	 * @param request
	 *            the profile update request
	 * @param userDetails
	 *            the authenticated user details
	 * @return the updated user profile
	 */
	@PatchMapping("/profile")
	@Operation(summary = "Update user profile", description = "Updates optional profile details for the authenticated user")
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Profile successfully updated"),
			@ApiResponse(responseCode = "401", description = "Unauthorized"),
			@ApiResponse(responseCode = "404", description = "User not found")})
	public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody ProfileUpdateRequest request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		UpdateProfileUseCase.UpdateProfileCommand command = new UpdateProfileUseCase.UpdateProfileCommand(
				userDetails.getId(), request.mobileNumber(), request.address());

		User updatedUser = updateProfileUseCase.updateProfile(command);
		return ResponseEntity.ok(toUserResponse(updatedUser));
	}

	private UserResponse toUserResponse(User user) {
		return new UserResponse(user.getId(), user.getEmail(), user.getProfile().getName(),
				user.getProfile().getSurname(), user.getProfile().getMobileNumber(), user.getProfile().getAddress());
	}
}
