package com.pilo.documents;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentExtractionRepository extends JpaRepository<DocumentExtraction, UUID> {

	@Query("""
			SELECT de FROM DocumentExtraction de
			JOIN FETCH de.document
			WHERE de.document.id IN :documentIds
			""")
	List<DocumentExtraction> findByDocumentIdIn(@Param("documentIds") List<UUID> documentIds);
}
