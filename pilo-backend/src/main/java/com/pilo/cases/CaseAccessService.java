package com.pilo.cases;

import com.pilo.users.Role;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CaseAccessService {

	private final ProcedureCaseRepository procedureCaseRepository;

	public CaseAccessService(ProcedureCaseRepository procedureCaseRepository) {
		this.procedureCaseRepository = procedureCaseRepository;
	}

	public ProcedureCase requireAccessibleCase(UUID caseId, UUID userId, Role role) {
		ProcedureCase procedureCase = procedureCaseRepository
				.findDetailedById(caseId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CASE_NOT_FOUND"));

		if (role == Role.ADMIN || role == Role.REVIEWER) {
			return procedureCase;
		}

		if (!procedureCase.getUser().getId().equals(userId)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "CASE_FORBIDDEN");
		}

		return procedureCase;
	}
}
