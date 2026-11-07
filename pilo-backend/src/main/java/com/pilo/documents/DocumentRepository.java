package com.pilo.documents;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

	@Query("""
			SELECT d FROM Document d
			JOIN FETCH d.requirement
			WHERE d.procedureCase.id = :caseId
			ORDER BY d.createdAt DESC
			""")
	List<Document> findByCaseId(@Param("caseId") UUID caseId);

	@Query("""
			SELECT d FROM Document d
			JOIN FETCH d.requirement
			JOIN FETCH d.procedureCase pc
			WHERE pc.id IN :caseIds
			""")
	List<Document> findByCaseIdIn(@Param("caseIds") Collection<UUID> caseIds);

	Optional<Document> findByIdAndProcedureCaseId(UUID id, UUID caseId);

	boolean existsByRequirementId(UUID requirementId);

	@Query("""
			SELECT d FROM Document d
			JOIN FETCH d.procedureCase pc
			JOIN FETCH pc.user
			JOIN FETCH d.requirement
			WHERE d.id = :id
			""")
	Optional<Document> findForProcessing(@Param("id") UUID id);
}
