package com.pilo.users;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pilo.support.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class AdminUserIT extends AbstractIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void seed() {
		persist("admin@pilo.test", "Marta Admin", Role.ADMIN);
		persist("user@pilo.test", "Ana Usuario", Role.USER);
	}

	@Test
	void adminCanDisableUserAndLoginFails() throws Exception {
		String adminToken = login("admin@pilo.test");
		User target = userRepository.findByEmailIgnoreCase("user@pilo.test").orElseThrow();

		mockMvc.perform(get("/api/v1/admin/users").header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.email=='user@pilo.test')].enabled").value(true));

		mockMvc.perform(post("/api/v1/admin/users/" + target.getId() + "/enabled")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"enabled\":false}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.enabled").value(false));

		mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"user@pilo.test","password":"Password123!"}
								"""))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void adminCannotDisableSelf() throws Exception {
		String adminToken = login("admin@pilo.test");
		User admin = userRepository.findByEmailIgnoreCase("admin@pilo.test").orElseThrow();

		mockMvc.perform(post("/api/v1/admin/users/" + admin.getId() + "/enabled")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"enabled\":false}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("CANNOT_DISABLE_SELF"));
	}

	private void persist(String email, String fullName, Role role) {
		User user = new User();
		user.setEmail(email);
		user.setFullName(fullName);
		user.setRole(role);
		user.setPasswordHash(passwordEncoder.encode("Password123!"));
		userRepository.save(user);
	}

	private String login(String email) throws Exception {
		String body = mockMvc.perform(post("/api/v1/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"" + email + "\",\"password\":\"Password123!\"}"))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
		return body.replaceAll(".*\"accessToken\":\"([^\"]+)\".*", "$1");
	}
}
