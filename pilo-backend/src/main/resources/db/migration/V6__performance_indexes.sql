-- Speeds up "my cases" listing, which filters by user_id and orders by created_at DESC.
CREATE INDEX idx_procedure_cases_user_id_created_at ON procedure_cases (user_id, created_at DESC);

-- Speeds up existsByRequirementId() lookups used when deleting a requirement from the admin catalog.
CREATE INDEX idx_documents_requirement_id ON documents (requirement_id);

-- Speeds up audit event lookups scoped to a resource, ordered by recency.
CREATE INDEX idx_audit_events_resource_timestamp ON audit_events (resource, timestamp DESC);
