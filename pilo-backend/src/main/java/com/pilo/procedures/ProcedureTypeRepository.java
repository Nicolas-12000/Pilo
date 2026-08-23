package com.pilo.procedures;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProcedureTypeRepository extends JpaRepository<ProcedureType, UUID> {

	@Query("""
			SELECT DISTINCT pt FROM ProcedureType pt
			LEFT JOIN FETCH pt.requirements
			ORDER BY pt.title ASC
			""")
	List<ProcedureType> findAllWithRequirements();

	@Query("""
			SELECT DISTINCT pt FROM ProcedureType pt
			LEFT JOIN FETCH pt.requirements
			WHERE pt.id = :id
			""")
	Optional<ProcedureType> findByIdWithRequirements(@Param("id") UUID id);
}
