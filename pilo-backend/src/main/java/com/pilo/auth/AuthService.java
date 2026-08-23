package com.pilo.auth;

import com.pilo.users.User;
import com.pilo.users.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

	private static final String DUMMY_BCRYPT_HASH =
			"$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final JwtProperties jwtProperties;

	public AuthService(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			JwtService jwtService,
			JwtProperties jwtProperties) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.jwtProperties = jwtProperties;
	}

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		User user = userRepository.findByEmailIgnoreCase(request.email()).orElse(null);
		String hash = user != null ? user.getPasswordHash() : DUMMY_BCRYPT_HASH;
		boolean matches = passwordEncoder.matches(request.password(), hash);
		if (user == null || !matches) {
			throw new InvalidCredentialsException();
		}
		return new LoginResponse(
				jwtService.createAccessToken(user),
				"Bearer",
				jwtProperties.expiration().toSeconds(),
				UserResponse.from(user));
	}
}
