package com.pilo.procedures;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pilo.common.CacheNames;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Transactional
class ProcedureTypeControllerIT extends ProcedureCatalogIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CacheManager cacheManager;

	@Test
	void listProcedureTypesWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/v1/procedures/types"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(procedureTypeId.toString()))
				.andExpect(jsonPath("$[0].title").value("Registro de contrato de alquiler"))
				.andExpect(jsonPath("$[0].targetDays").value(15))
				.andExpect(jsonPath("$[0].requirementCount").value(3));
	}

	@Test
	void getProcedureTypeDetailWithoutAuthentication() throws Exception {
		mockMvc.perform(get("/api/v1/procedures/types/{id}", procedureTypeId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Registro de contrato de alquiler"))
				.andExpect(jsonPath("$.requirements.length()").value(3))
				.andExpect(jsonPath("$.requirements[0].code").value("HOME_INSURANCE"))
				.andExpect(jsonPath("$.requirements[0].mandatory").value(false))
				.andExpect(jsonPath("$.requirements[1].code").value("IDENTITY_DOCUMENT"));
	}

	@Test
	void archivedProcedureTypeLeavesPublicCatalogue() throws Exception {
		ProcedureType procedureType = procedureTypeRepository.findById(procedureTypeId).orElseThrow();
		procedureType.setArchivedAt(Instant.now());
		procedureTypeRepository.saveAndFlush(procedureType);
		cacheManager.getCache(CacheNames.PROCEDURE_CATALOG).clear();
		try {
			mockMvc.perform(get("/api/v1/procedures/types"))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.length()").value(0));
			mockMvc.perform(get("/api/v1/procedures/types/{id}", procedureTypeId))
					.andExpect(status().isNotFound());
		} finally {
			// The transaction rolls back the archive; the cache would otherwise keep the empty list.
			cacheManager.getCache(CacheNames.PROCEDURE_CATALOG).clear();
		}
	}

	@Test
	void getUnknownProcedureTypeReturnsNotFound() throws Exception {
		mockMvc.perform(get("/api/v1/procedures/types/{id}", UUID.randomUUID()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.error").value("PROCEDURE_TYPE_NOT_FOUND"));
	}
}
