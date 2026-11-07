package com.pilo.auth.application;

import com.pilo.auth.AuthService;
import com.pilo.auth.ChangePasswordRequest;
import com.pilo.auth.LoginRequest;
import com.pilo.auth.LoginResponse;
import com.pilo.auth.UpdateProfileRequest;
import com.pilo.auth.UserResponse;
import com.pilo.users.User;
import com.pilo.users.UserRepository;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthAccountUseCase {

	private final AuthService authService;
	private final UserRepository userRepository;

	public AuthAccountUseCase(AuthService authService, UserRepository userRepository) {
		this.authService = authService;
		this.userRepository = userRepository;
	}

	public LoginResponse login(LoginRequest request) {
		return authService.login(request);
	}

	public UserResponse getCurrentUser(UUID userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
		return UserResponse.from(user);
	}

	public UserResponse updateProfile(UUID userId, UpdateProfileRequest request) {
		return authService.updateProfile(userId, request);
	}

	public LoginResponse changePassword(UUID userId, ChangePasswordRequest request) {
		return authService.changePassword(userId, request);
	}
}
