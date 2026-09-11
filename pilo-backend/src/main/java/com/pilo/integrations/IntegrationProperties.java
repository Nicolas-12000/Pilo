package com.pilo.integrations;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pilo.integrations")
public record IntegrationProperties(
		String boeBaseUrl,
		String datosBaseUrl,
		String siaBaseUrl,
		Duration timeout,
		boolean demoFallbackEnabled) {

	public IntegrationProperties {
		if (boeBaseUrl == null || boeBaseUrl.isBlank()) {
			boeBaseUrl = "https://www.boe.es/datosabiertos/api/boe/sumario";
		}
		if (datosBaseUrl == null || datosBaseUrl.isBlank()) {
			datosBaseUrl = "https://datos.gob.es/apidata/catalog/dataset";
		}
		if (siaBaseUrl == null || siaBaseUrl.isBlank()) {
			siaBaseUrl = "https://datos.gob.es/apidata/catalog/dataset";
		}
		if (timeout == null) {
			timeout = Duration.ofSeconds(4);
		}
	}
}
