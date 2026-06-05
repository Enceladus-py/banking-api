package com.example.demo.user.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.common.domain.exception.EntityNotFoundException;
import com.example.demo.user.application.port.in.GetUserUseCase;
import com.example.demo.user.application.port.in.RegisterUserUseCase;
import com.example.demo.user.domain.model.Profile;
import com.example.demo.user.domain.model.User;
import com.example.demo.common.security.SecurityConfig;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private RegisterUserUseCase registerUserUseCase;

	@MockitoBean
	private GetUserUseCase getUserUseCase;

	@Test
	void shouldRegisterUserSuccessfully() throws Exception {
		User user = new User("uuid-123", "alice@example.com", "pwd", new Profile("Alice", "Smith"), 1L);
		when(registerUserUseCase.registerUser(any(RegisterUserUseCase.RegisterUserCommand.class))).thenReturn(user);

		String jsonPayload = """
				{
				    "email": "alice@example.com",
				    "password": "password",
				    "name": "Alice",
				    "surname": "Smith"
				}
				""";

		mockMvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON).content(jsonPayload))
				.andExpect(status().isOk()).andExpect(jsonPath("$.id").value("uuid-123"))
				.andExpect(jsonPath("$.email").value("alice@example.com"))
				.andExpect(jsonPath("$.name").value("Alice")).andExpect(jsonPath("$.surname").value("Smith"));

		verify(registerUserUseCase).registerUser(any(RegisterUserUseCase.RegisterUserCommand.class));
	}

	@Test
	void shouldGetUserByIdSuccessfully() throws Exception {
		User user = new User("uuid-123", "alice@example.com", "pwd", new Profile("Alice", "Smith"), 1L);
		when(getUserUseCase.getUserById("uuid-123")).thenReturn(user);

		mockMvc.perform(get("/api/users/uuid-123")).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value("uuid-123")).andExpect(jsonPath("$.name").value("Alice"))
				.andExpect(jsonPath("$.surname").value("Smith"));

		verify(getUserUseCase).getUserById("uuid-123");
	}

	@Test
	void shouldReturn404NotFoundWhenUserDoesNotExist() throws Exception {
		when(getUserUseCase.getUserById("missing-uuid"))
				.thenThrow(new EntityNotFoundException("User not found with ID: missing-uuid"));

		mockMvc.perform(get("/api/users/missing-uuid")).andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("User not found with ID: missing-uuid"));

		verify(getUserUseCase).getUserById("missing-uuid");
	}
}
