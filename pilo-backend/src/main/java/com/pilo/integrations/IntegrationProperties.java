package com.pilo.integrations;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pilo.integrations")
public record IntegrationProperties(
		String boeLegislationUrl,
		String datosCatalogUrl,
		String siaPagSearchUrl,
		Duration timeout,
		boolean demoFallbackEnabled,
		int maxQueryLength) {

	public IntegrationProperties {
		if (boeLegislationUrl == null || boeLegislationUrl.isBlank()) {
			boeLegislationUrl = "https://boe.es/datosabiertos/api/legislacion-consolidada";
		}
		if (datosCatalogUrl == null || datosCatalogUrl.isBlank()) {
			datosCatalogUrl = "https://datos.gob.es/apidata/catalog/dataset";
		}
		if (siaPagSearchUrl == null || siaPagSearchUrl.isBlank()) {
			siaPagSearchUrl =
					"https://administracion.gob.es/pagFront/recursosAEM/recursos/tramites/busqueda.htm";
		}
		if (timeout == null) {
			timeout = Duration.ofSeconds(4);
		}
		if (maxQueryLength <= 0) {
			maxQueryLength = 120;
		}
	}
}
