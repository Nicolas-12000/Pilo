package com.pilo.audit;

import java.util.List;
import java.util.UUID;
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
}
