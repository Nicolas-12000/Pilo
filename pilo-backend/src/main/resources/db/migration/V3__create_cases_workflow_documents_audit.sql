CREATE TABLE procedure_cases (
    id UUID PRIMARY KEY,
    case_number VARCHAR(32) NOT NULL,
    user_id UUID NOT NULL REFERENCES users (id),
    procedure_type_id UUID NOT NULL REFERENCES procedure_types (id),
    status VARCHAR(32) NOT NULL,
    progress_percentage INTEGER NOT NULL DEFAULT 0,
    deadline_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_procedure_cases_case_number UNIQUE (case_number),
    CONSTRAINT ck_procedure_cases_status CHECK (
        status IN ('PENDING', 'IN_PROGRESS', 'UNDER_REVIEW', 'APPROVED', 'REJECTED')
    ),
    CONSTRAINT ck_procedure_cases_progress CHECK (
        progress_percentage >= 0 AND progress_percentage <= 100
    )
);

CREATE INDEX idx_procedure_cases_user_id ON procedure_cases (user_id);
CREATE INDEX idx_procedure_cases_procedure_type_id ON procedure_cases (procedure_type_id);

CREATE TABLE workflow_tasks (
    id UUID PRIMARY KEY,
    case_id UUID NOT NULL REFERENCES procedure_cases (id) ON DELETE CASCADE,
    task_name VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL,
    assigned_role VARCHAR(32) NOT NULL,
    depends_on_task_id UUID REFERENCES workflow_tasks (id),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_workflow_tasks_status CHECK (
        status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'BLOCKED')
    ),
    CONSTRAINT ck_workflow_tasks_role CHECK (assigned_role IN ('USER', 'REVIEWER', 'ADMIN'))
);

CREATE INDEX idx_workflow_tasks_case_id ON workflow_tasks (case_id);

CREATE TABLE documents (
    id UUID PRIMARY KEY,
    case_id UUID NOT NULL REFERENCES procedure_cases (id) ON DELETE CASCADE,
    requirement_id UUID NOT NULL REFERENCES requirements (id),
    file_name VARCHAR(255) NOT NULL,
    mime_type VARCHAR(128) NOT NULL,
    file_size BIGINT NOT NULL,
    storage_key VARCHAR(512) NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_documents_status CHECK (
        status IN ('UPLOADED', 'PROCESSING', 'VALIDATED', 'REJECTED', 'PROCESSING_FAILED')
    )
);

CREATE INDEX idx_documents_case_id ON documents (case_id);

CREATE TABLE document_extractions (
    id UUID PRIMARY KEY,
    document_id UUID NOT NULL REFERENCES documents (id) ON DELETE CASCADE,
    document_type_detected VARCHAR(128) NOT NULL,
    confidence DOUBLE PRECISION NOT NULL,
    extracted_data JSONB NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX uk_document_extractions_document_id ON document_extractions (document_id);

CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    actor_id UUID REFERENCES users (id),
    action VARCHAR(64) NOT NULL,
    resource VARCHAR(128) NOT NULL,
    result VARCHAR(32) NOT NULL,
    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
    timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_events_resource ON audit_events (resource);

CREATE SEQUENCE case_number_seq START WITH 1 INCREMENT BY 1;
