package com.pilo.procedures;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RequirementRepository extends JpaRepository<Requirement, UUID> {

	@Query("""
			SELECT r FROM Requirement r
			JOIN FETCH r.procedureType
			WHERE r.id = :id
			""")
	Optional<Requirement> findDetailedById(@Param("id") UUID id);
}
