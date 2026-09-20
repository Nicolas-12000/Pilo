package com.pilo.auth;

import com.pilo.ai.InternalApiProperties;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Profile("aws")
public class ProductionSecretsGuard implements ApplicationRunner {

	private static final String DEV_JWT_MARKER = "dev-only-change-me";
	private static final String DEV_INTERNAL_KEY = "dev-internal-key";

	private final JwtProperties jwtProperties;
	private final InternalApiProperties internalApiProperties;

	public ProductionSecretsGuard(JwtProperties jwtProperties, InternalApiProperties internalApiProperties) {
		this.jwtProperties = jwtProperties;
		this.internalApiProperties = internalApiProperties;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (!StringUtils.hasText(jwtProperties.secret())
				|| jwtProperties.secret().contains(DEV_JWT_MARKER)
				|| jwtProperties.secret().getBytes().length < 32) {
			throw new IllegalStateException("PILO_JWT_SECRET must be set to a unique 32+ byte value on aws");
		}
		if (!StringUtils.hasText(internalApiProperties.apiKey())
				|| DEV_INTERNAL_KEY.equals(internalApiProperties.apiKey())) {
			throw new IllegalStateException("PILO_INTERNAL_API_KEY must be set to a unique value on aws");
		}
	}
}
