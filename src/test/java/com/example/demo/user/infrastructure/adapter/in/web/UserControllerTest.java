package com.example.demo.user.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.common.domain.exception.EntityNotFoundException;
import com.example.demo.common.infrastructure.security.CustomUserDetails;
import com.example.demo.common.infrastructure.security.JwtService;
import com.example.demo.common.infrastructure.security.SecurityConfig;
import com.example.demo.user.application.port.in.GetUserUseCase;
import com.example.demo.user.application.port.in.RegisterUserUseCase;
import com.example.demo.user.application.port.in.UpdateProfileUseCase;
import com.example.demo.user.domain.model.Profile;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private RegisterUserUseCase registerUserUseCase;

	@MockitoBean
	private GetUserUseCase getUserUseCase;

	@MockitoBean
	private UpdateProfileUseCase updateProfileUseCase;

	@MockitoBean
	private AuthenticationManager authenticationManager;

	@MockitoBean
	private JwtService jwtService;

	@MockitoBean
	private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

	@Test
	void shouldLoginUserSuccessfully() throws Exception {
		String jsonPayload = """
				{
				    "email": "alice@example.com",
				    "password": "password"
				}
				""";

		UserDetails userDetails = User.builder().username("alice@example.com").password("password").authorities("USER")
				.build();
		Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
		when(jwtService.generateToken(userDetails)).thenReturn("mocked.access.token");
		when(jwtService.generateRefreshToken(userDetails)).thenReturn("mocked.refresh.token");

		mockMvc.perform(post("/api/users/login").contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
				.andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").value("mocked.access.token"))
				.andExpect(jsonPath("$.refreshToken").value("mocked.refresh.token"));

		verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
		verify(jwtService).generateToken(userDetails);
		verify(jwtService).generateRefreshToken(userDetails);
	}

	@Test
	void shouldRefreshTokenSuccessfully() throws Exception {
		String jsonPayload = """
				{
				    "refreshToken": "valid.refresh.token"
				}
				""";

		UserDetails userDetails = User.builder().username("alice@example.com").password("password").authorities("USER")
				.build();

		when(jwtService.extractUsername("valid.refresh.token")).thenReturn("alice@example.com");
		when(userDetailsService.loadUserByUsername("alice@example.com")).thenReturn(userDetails);
		when(jwtService.isTokenValid("valid.refresh.token", userDetails)).thenReturn(true);
		when(jwtService.generateToken(userDetails)).thenReturn("new.access.token");
		when(jwtService.generateRefreshToken(userDetails)).thenReturn("new.refresh.token");

		mockMvc.perform(post("/api/users/refresh").contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
				.andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").value("new.access.token"))
				.andExpect(jsonPath("$.refreshToken").value("new.refresh.token"));
	}

	@Test
	void shouldRegisterUserSuccessfully() throws Exception {
		com.example.demo.user.domain.model.User user = com.example.demo.user.domain.model.User.reconstitute("uuid-123",
				"alice@example.com", "pwd", Profile.createNew("Alice", "Smith"), 1L);
		when(registerUserUseCase.registerUser(any(RegisterUserUseCase.RegisterUserCommand.class))).thenReturn(user);

		String jsonPayload = """
				{
				    "email": "alice@example.com", "password": "password", "name": "Alice",
				    "surname": "Smith"
				}
				""";

		mockMvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
				.andExpect(status().isOk()).andExpect(jsonPath("$.id").value("uuid-123"))
				.andExpect(jsonPath("$.email").value("alice@example.com")).andExpect(jsonPath("$.name").value("Alice"))
				.andExpect(jsonPath("$.surname").value("Smith"));

		verify(registerUserUseCase).registerUser(any(RegisterUserUseCase.RegisterUserCommand.class));
	}

	@Test
	void shouldGetUserProfileSuccessfully() throws Exception {
		com.example.demo.user.domain.model.User user = com.example.demo.user.domain.model.User.reconstitute("uuid-123",
				"alice@example.com", "pwd", Profile.createNew("Alice", "Smith"), 1L);
		when(getUserUseCase.getUserById("uuid-123")).thenReturn(user);

		mockMvc.perform(get("/api/users/profile")
				.with(user(new CustomUserDetails("uuid-123", "alice@example.com", "pwd", Collections.emptyList()))))
				.andExpect(status().isOk()).andExpect(jsonPath("$.id").value("uuid-123"))
				.andExpect(jsonPath("$.name").value("Alice")).andExpect(jsonPath("$.surname").value("Smith"));

		verify(getUserUseCase).getUserById("uuid-123");
	}

	@Test
	void shouldReturn404NotFoundWhenUserProfileDoesNotExist() throws Exception {
		when(getUserUseCase.getUserById("missing-uuid"))
				.thenThrow(new EntityNotFoundException("User not found with id: missing-uuid"));

		mockMvc.perform(get("/api/users/profile")
				.with(user(new CustomUserDetails("missing-uuid", "alice@example.com", "pwd", Collections.emptyList()))))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("User not found with id: missing-uuid"));

		verify(getUserUseCase).getUserById("missing-uuid");
	}

	@Test
	void shouldUpdateProfileSuccessfully() throws Exception {
		String jsonPayload = """
				{
				    "mobileNumber": "+1234567890",
				    "address": "123 Main St"
				}
				""";

		com.example.demo.user.domain.model.User updatedUser = com.example.demo.user.domain.model.User.reconstitute(
				"uuid-123", "alice@example.com", "pwd",
				Profile.reconstitute("uuid-profile-1", "Alice", "Smith", "+1234567890", "123 Main St"), 1L);

		when(updateProfileUseCase.updateProfile(any(UpdateProfileUseCase.UpdateProfileCommand.class)))
				.thenReturn(updatedUser);

		mockMvc.perform(patch("/api/users/profile")
				.with(user(new CustomUserDetails("uuid-123", "alice@example.com", "pwd", Collections.emptyList())))
				.contentType(MediaType.APPLICATION_JSON).content(jsonPayload)).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("uuid-123"))
				.andExpect(jsonPath("$.mobileNumber").value("+1234567890"))
				.andExpect(jsonPath("$.address").value("123 Main St"));

		verify(updateProfileUseCase).updateProfile(any(UpdateProfileUseCase.UpdateProfileCommand.class));
	}

	@Test
	void shouldReturn401WhenRefreshTokenUsernameIsNull() throws Exception {
		String jsonPayload = """
				{
				    "refreshToken": "invalid.refresh.token"
				}
				""";

		when(jwtService.extractUsername("invalid.refresh.token")).thenReturn(null);

		mockMvc.perform(post("/api/users/refresh").contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void shouldReturn401WhenRefreshTokenIsInvalid() throws Exception {
		String jsonPayload = """
				{
				    "refreshToken": "invalid.refresh.token"
				}
				""";

		UserDetails userDetails = User.builder().username("alice@example.com").password("password").authorities("USER")
				.build();

		when(jwtService.extractUsername("invalid.refresh.token")).thenReturn("alice@example.com");
		when(userDetailsService.loadUserByUsername("alice@example.com")).thenReturn(userDetails);
		when(jwtService.isTokenValid("invalid.refresh.token", userDetails)).thenReturn(false);

		mockMvc.perform(post("/api/users/refresh").contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
				.andExpect(status().isUnauthorized());
	}
}
