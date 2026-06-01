package com.example.demo.infrastructure.adapter.in.web;

import com.example.demo.application.port.in.GetUserUseCase;
import com.example.demo.application.port.in.RegisterUserUseCase;
import com.example.demo.domain.model.User;
import com.example.demo.domain.exception.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterUserUseCase registerUserUseCase;

    @MockitoBean
    private GetUserUseCase getUserUseCase;

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {
        User user = new User("uuid-123", "Alice", "Smith", 1L);
        when(registerUserUseCase.registerUser(any(RegisterUserUseCase.RegisterUserCommand.class)))
                .thenReturn(user);

        String jsonPayload = """
                {
                    "name": "Alice",
                    "surname": "Smith"
                }
                """;

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("uuid-123"))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.surname").value("Smith"));

        verify(registerUserUseCase).registerUser(any(RegisterUserUseCase.RegisterUserCommand.class));
    }

    @Test
    void shouldGetUserByIdSuccessfully() throws Exception {
        User user = new User("uuid-123", "Alice", "Smith", 1L);
        when(getUserUseCase.getUserById("uuid-123")).thenReturn(user);

        mockMvc.perform(get("/api/users/uuid-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("uuid-123"))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.surname").value("Smith"));

        verify(getUserUseCase).getUserById("uuid-123");
    }

    @Test
    void shouldReturn404NotFoundWhenUserDoesNotExist() throws Exception {
        when(getUserUseCase.getUserById("missing-uuid"))
                .thenThrow(new EntityNotFoundException("User not found with ID: missing-uuid"));

        mockMvc.perform(get("/api/users/missing-uuid"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found with ID: missing-uuid"));

        verify(getUserUseCase).getUserById("missing-uuid");
    }
}
