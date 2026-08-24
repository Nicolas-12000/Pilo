package com.pilo.workflows;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkflowTaskRepository extends JpaRepository<WorkflowTask, UUID> {

	@Query("""
			SELECT wt FROM WorkflowTask wt
			LEFT JOIN FETCH wt.dependsOnTask
			WHERE wt.procedureCase.id = :caseId
			""")
	List<WorkflowTask> findByCaseId(@Param("caseId") UUID caseId);
}
