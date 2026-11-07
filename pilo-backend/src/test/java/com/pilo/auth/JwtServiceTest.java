package com.pilo.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pilo.users.Role;
import com.pilo.users.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

	private static final String SECRET = "test-secret-must-be-at-least-32-bytes-long!";
	private static final String PREVIOUS = "previous-secret-must-be-at-least-32-bytes!";

	@Test
	void createsTokenWithUserClaims() {
		JwtService jwtService = new JwtService(JwtProperties.forTest(SECRET, Duration.ofHours(1)));
		User user = user("ana@pilo.test", Role.USER);

		String token = jwtService.createAccessToken(user);
		JwtPrincipal principal = jwtService.parse(token);

		assertThat(principal.userId()).isEqualTo(user.getId());
		assertThat(principal.email()).isEqualTo("ana@pilo.test");
		assertThat(principal.role()).isEqualTo(Role.USER);
		assertThat(principal.tokenVersion()).isZero();
	}

	@Test
	void rejectsExpiredToken() throws InterruptedException {
		JwtService jwtService = new JwtService(JwtProperties.forTest(SECRET, Duration.ofMillis(1)));
		String token = jwtService.createAccessToken(user("ana@pilo.test", Role.ADMIN));

		Thread.sleep(20);

		assertThatThrownBy(() -> jwtService.parse(token)).isInstanceOf(ExpiredJwtException.class);
	}

	@Test
	void rejectsTamperedToken() {
		JwtService jwtService = new JwtService(JwtProperties.forTest(SECRET, Duration.ofHours(1)));
		String token = jwtService.createAccessToken(user("ana@pilo.test", Role.REVIEWER));

		assertThatThrownBy(() -> jwtService.parse(token + "x")).isInstanceOf(JwtException.class);
	}

	@Test
	void acceptsTokenSignedWithPreviousSecretDuringRotation() {
		JwtService oldIssuer = new JwtService(JwtProperties.forTest(PREVIOUS, Duration.ofHours(1)));
		String token = oldIssuer.createAccessToken(user("ana@pilo.test", Role.USER));

		JwtService rotated = new JwtService(
				new JwtProperties(SECRET, PREVIOUS, Duration.ofHours(1), "pilo", "pilo-api"));
		JwtPrincipal principal = rotated.parse(token);

		assertThat(principal.email()).isEqualTo("ana@pilo.test");
	}

	@Test
	void rejectsPreviousSecretAfterItIsDropped() {
		JwtService oldIssuer = new JwtService(JwtProperties.forTest(PREVIOUS, Duration.ofHours(1)));
		String token = oldIssuer.createAccessToken(user("ana@pilo.test", Role.USER));

		JwtService rotated = new JwtService(JwtProperties.forTest(SECRET, Duration.ofHours(1)));

		assertThatThrownBy(() -> rotated.parse(token)).isInstanceOf(JwtException.class);
	}

	private static User user(String email, Role role) {
		User user = new User();
		user.setId(UUID.randomUUID());
		user.setEmail(email);
		user.setFullName("Ana Usuario");
		user.setRole(role);
		user.setPasswordHash("unused");
		return user;
	}
}
