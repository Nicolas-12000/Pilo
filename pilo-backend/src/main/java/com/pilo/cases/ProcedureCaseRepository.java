package com.pilo.cases;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProcedureCaseRepository extends JpaRepository<ProcedureCase, UUID> {

	@Query("""
			SELECT pc FROM ProcedureCase pc
			JOIN FETCH pc.procedureType
			WHERE pc.user.id = :userId
			ORDER BY pc.createdAt DESC
			""")
	List<ProcedureCase> findByUserId(@Param("userId") UUID userId);

	@Query("""
			SELECT pc FROM ProcedureCase pc
			JOIN FETCH pc.procedureType pt
			LEFT JOIN FETCH pt.requirements
			JOIN FETCH pc.user
			WHERE pc.id = :id
			""")
	Optional<ProcedureCase> findDetailedById(@Param("id") UUID id);

	Optional<ProcedureCase> findByCaseNumber(String caseNumber);
}
