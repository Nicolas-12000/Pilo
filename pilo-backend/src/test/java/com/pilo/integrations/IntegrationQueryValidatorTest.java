package com.pilo.integrations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class IntegrationQueryValidatorTest {

	private IntegrationQueryValidator validator;

	@BeforeEach
	void setUp() {
		validator = new IntegrationQueryValidator(new IntegrationProperties(null, null, null, Duration.ofSeconds(4), true, 120));
	}

	@Test
	void acceptsNormalizedQuery() {
		assertThat(validator.requireValid("  empadronamiento municipal  "))
				.isEqualTo("empadronamiento municipal");
	}

	@Test
	void rejectsBlankQuery() {
		assertThatThrownBy(() -> validator.requireValid("   "))
				.isInstanceOf(ResponseStatusException.class)
				.extracting("statusCode")
				.isEqualTo(HttpStatus.BAD_REQUEST);
	}

	@Test
	void rejectsUnsafeCharacters() {
		assertThatThrownBy(() -> validator.requireValid("empadronamiento<script>"))
				.isInstanceOf(ResponseStatusException.class);
	}
}
