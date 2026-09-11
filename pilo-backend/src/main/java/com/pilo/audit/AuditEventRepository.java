package com.pilo.audit;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {

	@Query("""
			SELECT ae FROM AuditEvent ae
			WHERE ae.resource LIKE :resourcePrefix%
			ORDER BY ae.timestamp DESC
			""")
	List<AuditEvent> findByResourcePrefix(@Param("resourcePrefix") String resourcePrefix);

	@Query("""
			SELECT ae FROM AuditEvent ae
			WHERE ae.resource IN :resources
			ORDER BY ae.timestamp DESC
			""")
	List<AuditEvent> findByResources(@Param("resources") List<String> resources, PageRequest pageRequest);

	@Query("""
			SELECT ae FROM AuditEvent ae
			WHERE ae.resource IN :resources AND ae.action = :action
			ORDER BY ae.timestamp DESC
			""")
	List<AuditEvent> findLatestByResourcesAndAction(
			@Param("resources") List<String> resources, @Param("action") String action);
}
