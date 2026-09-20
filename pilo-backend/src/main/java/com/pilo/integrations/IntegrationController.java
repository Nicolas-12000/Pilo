package com.pilo.integrations;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/integrations")
@Validated
public class IntegrationController {

	private final IntegrationService integrationService;
	private final IntegrationQueryValidator integrationQueryValidator;

	public IntegrationController(
			IntegrationService integrationService, IntegrationQueryValidator integrationQueryValidator) {
		this.integrationService = integrationService;
		this.integrationQueryValidator = integrationQueryValidator;
	}

	@GetMapping("/boe/search")
	public List<ExternalReferenceResponse> searchBoe(
			@RequestParam @NotBlank @Size(max = 120) String query) {
		return integrationService.searchBoe(integrationQueryValidator.requireValid(query));
	}

	@GetMapping("/datos/search")
	public List<ExternalReferenceResponse> searchDatos(
			@RequestParam @NotBlank @Size(max = 120) String query) {
		return integrationService.searchDatos(integrationQueryValidator.requireValid(query));
	}

	@GetMapping("/sia/search")
	public List<ExternalReferenceResponse> searchSia(
			@RequestParam @NotBlank @Size(max = 120) String query) {
		return integrationService.searchSia(integrationQueryValidator.requireValid(query));
	}
}
