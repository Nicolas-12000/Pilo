package com.pilo.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pilo.support.AbstractIntegrationTest;
import com.pilo.users.Role;
import com.pilo.users.User;
import com.pilo.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class AuthControllerIT extends AbstractIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void seedUser() {
		User user = new User();
		user.setEmail("user@pilo.test");
		user.setFullName("Ana Usuario");
		user.setRole(Role.USER);
		user.setPasswordHash(passwordEncoder.encode("Password123!"));
		userRepository.save(user);
	}

	@Test
	void loginReturnsAccessTokenForValidCredentials() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":"user@pilo.test","password":"Password123!"}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.user.email").value("user@pilo.test"))
				.andExpect(jsonPath("$.user.fullName").value("Ana Usuario"))
				.andExpect(jsonPath("$.user.role").value("USER"));
	}

	@Test
	void loginRejectsUnknownEmail() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":"missing@pilo.test","password":"Password123!"}
						"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
	}

	@Test
	void loginRejectsWrongPassword() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":"user@pilo.test","password":"WrongPassword!"}
						"""))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
	}

	@Test
	void loginRejectsInvalidPayload() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":"not-an-email","password":""}
						"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
	}

	@Test
	void meRequiresAuthentication() throws Exception {
		mockMvc.perform(get("/api/v1/auth/me")).andExpect(status().isUnauthorized());
	}

	@Test
	void meReturnsCurrentUserWhenTokenIsValid() throws Exception {
		String body = mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":"user@pilo.test","password":"Password123!"}
						"""))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();

		String token = body.replaceAll(".*\"accessToken\":\"([^\"]+)\".*", "$1");

		mockMvc.perform(get("/api/v1/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("user@pilo.test"))
				.andExpect(jsonPath("$.role").value("USER"));
	}

	@Test
	void loginPreflightFromFrontendOriginIsAllowed() throws Exception {
		mockMvc.perform(options("/api/v1/auth/login")
				.header(HttpHeaders.ORIGIN, "http://localhost:3000")
				.header("Access-Control-Request-Method", "POST")
				.header("Access-Control-Request-Headers", "content-type"))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"))
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
	}

	@Test
	void loginResponseIncludesCorsHeadersForFrontend() throws Exception {
		mockMvc.perform(post("/api/v1/auth/login")
				.header(HttpHeaders.ORIGIN, "http://localhost:3000")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":"user@pilo.test","password":"Password123!"}
						"""))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"));
	}
}
