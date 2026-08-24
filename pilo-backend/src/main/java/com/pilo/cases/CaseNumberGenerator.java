package com.pilo.cases;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

@Component
public class CaseNumberGenerator {

	private final EntityManager entityManager;

	public CaseNumberGenerator(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	public String nextCaseNumber() {
		Number sequenceValue =
				(Number) entityManager.createNativeQuery("SELECT nextval('case_number_seq')").getSingleResult();
		return "PILO-%d".formatted(sequenceValue.longValue());
	}
}
