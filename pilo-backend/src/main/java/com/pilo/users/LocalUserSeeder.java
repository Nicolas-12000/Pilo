package com.pilo.users;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(SeedProperties.class)
@ConditionalOnProperty(prefix = "pilo.seed", name = "enabled", havingValue = "true")
public class LocalUserSeeder implements ApplicationRunner {

	private static final String LOCAL_PASSWORD = "Password123!";

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public LocalUserSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(ApplicationArguments args) {
		createIfMissing("user@pilo.test", "Ana Usuario", Role.USER);
		createIfMissing("reviewer@pilo.test", "Luis Revisor", Role.REVIEWER);
		createIfMissing("admin@pilo.test", "Marta Admin", Role.ADMIN);
	}

	private void createIfMissing(String email, String fullName, Role role) {
		if (userRepository.findByEmailIgnoreCase(email).isPresent()) {
			return;
		}
		User user = new User();
		user.setEmail(email);
		user.setFullName(fullName);
		user.setRole(role);
		user.setPasswordHash(passwordEncoder.encode(LOCAL_PASSWORD));
		userRepository.save(user);
	}
}
