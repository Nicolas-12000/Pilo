package com.pilo.auth;

import com.pilo.users.User;
import com.pilo.users.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthService authService;
	private final UserRepository userRepository;
	private final LoginRateLimiter loginRateLimiter;

	public AuthController(
			AuthService authService, UserRepository userRepository, LoginRateLimiter loginRateLimiter) {
		this.authService = authService;
		this.userRepository = userRepository;
		this.loginRateLimiter = loginRateLimiter;
	}

	@PostMapping("/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
		// Uses the direct socket address rather than X-Forwarded-For: trusting a client-supplied
		// header here would let an attacker spoof a fresh IP on every request and bypass the limiter.
		// If this app is deployed behind a trusted reverse proxy, resolve the real client IP there
		// (e.g. via server.forward-headers-strategy) instead of trusting the header per-request.
		if (!loginRateLimiter.tryAcquire(httpRequest.getRemoteAddr())) {
			throw new TooManyLoginAttemptsException();
		}
		return authService.login(request);
	}

	@GetMapping("/me")
	@ResponseStatus(HttpStatus.OK)
	public UserResponse me(Authentication authentication) {
		UUID userId = UUID.fromString(authentication.getName());
		User user = userRepository
				.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
		return UserResponse.from(user);
	}

	@PatchMapping("/me")
	public UserResponse updateProfile(
			Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {
		UUID userId = UUID.fromString(authentication.getName());
		return authService.updateProfile(userId, request);
	}

	@PostMapping("/me/password")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void changePassword(
			Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
		UUID userId = UUID.fromString(authentication.getName());
		authService.changePassword(userId, request);
	}
}
