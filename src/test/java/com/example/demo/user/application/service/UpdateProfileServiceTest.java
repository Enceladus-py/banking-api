package com.example.demo.user.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.common.domain.exception.EntityNotFoundException;
import com.example.demo.user.application.port.in.UpdateProfileUseCase.UpdateProfileCommand;
import com.example.demo.user.application.port.out.UserRepository;
import com.example.demo.user.domain.model.Profile;
import com.example.demo.user.domain.model.User;

@ExtendWith(MockitoExtension.class)
class UpdateProfileServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UpdateProfileService updateProfileService;

	private User user;

	@BeforeEach
	void setUp() {
		user = new User("user-1", "test@test.com", "pass", new Profile("profile-1", "John", "Doe", null, null), 1L);
	}

	@Test
	void shouldUpdateProfileWhenUserExists() {
		UpdateProfileCommand command = new UpdateProfileCommand("user-1", "+123456", "Main St");
		when(userRepository.findById("user-1")).thenReturn(Optional.of(user));
		when(userRepository.save(any(User.class))).thenReturn(user);

		User updatedUser = updateProfileService.updateProfile(command);

		assertThat(updatedUser.getProfile().getMobileNumber()).isEqualTo("+123456");
		assertThat(updatedUser.getProfile().getAddress()).isEqualTo("Main St");
		verify(userRepository).save(user);
	}

	@Test
	void shouldThrowExceptionWhenUserDoesNotExist() {
		UpdateProfileCommand command = new UpdateProfileCommand("missing-user", "+123456", "Main St");
		when(userRepository.findById("missing-user")).thenReturn(Optional.empty());

		assertThrows(EntityNotFoundException.class, () -> updateProfileService.updateProfile(command));
		verify(userRepository, never()).save(any());
	}
}
