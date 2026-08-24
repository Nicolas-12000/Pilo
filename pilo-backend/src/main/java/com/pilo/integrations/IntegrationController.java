package com.pilo.integrations;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/integrations")
public class IntegrationController {

	private final IntegrationService integrationService;

	public IntegrationController(IntegrationService integrationService) {
		this.integrationService = integrationService;
	}

	@GetMapping("/boe/search")
	public List<ExternalReferenceResponse> searchBoe(@RequestParam String query) {
		return integrationService.searchBoe(query);
	}

	@GetMapping("/datos/search")
	public List<ExternalReferenceResponse> searchDatos(@RequestParam String query) {
		return integrationService.searchDatos(query);
	}
}
